# 图节点架构改进建议

## 当前架构分析

### 现状
当前在图构建时，执行策略、Task 和 Agent 是分开处理的：

```java
// GraphBuilder.java 第 101-102 行
for (Task task : taskPlan.getTasks()) {
    TaskExecutor executor = ExecutorFactory.create(task, workersByID.get(task.getAgentId()), userContext);
    graph.addNode(task.getId(), state -> { ... });
}
```

**问题**:
1. 节点信息分散：Task、ExecutionStrategy、Agent 信息分散在不同对象中
2. 查找效率：每次都要通过 `task.getAgentId()` 查找 ServiceNode
3. 可扩展性：添加新的节点类型或策略需要修改多个地方
4. 调试困难：节点信息不集中，难以追踪和调试

## 改进方案

### 方案一：创建 GraphNode 封装类（推荐）

创建一个 `GraphNode` 类来封装图节点的所有信息：

```java
package com.example.ddd.domain.agent.service.execute.graph;

import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.executor.ExecutionStrategy;
import com.example.ddd.domain.agent.service.execute.executor.TaskExecutor;
import com.example.ddd.domain.agent.service.execute.task.Task;
import lombok.Builder;
import lombok.Data;

/**
 * 图节点封装
 * 将 Task、ExecutionStrategy、Agent 组合在一起
 */
@Data
@Builder
public class GraphNode {
    /**
     * 任务信息
     */
    private Task task;
    
    /**
     * 执行策略
     */
    private ExecutionStrategy executionStrategy;
    
    /**
     * Agent 服务节点
     */
    private ServiceNode agentNode;
    
    /**
     * 执行器实例（延迟创建）
     */
    private TaskExecutor executor;
    
    /**
     * 节点ID（通常等于 task.getId()）
     */
    public String getId() {
        return task != null ? task.getId() : null;
    }
    
    /**
     * Agent ID
     */
    public Long getAgentId() {
        return task != null ? task.getAgentId() : null;
    }
    
    /**
     * 创建执行器（延迟初始化）
     */
    public TaskExecutor getExecutor(UserContext userContext) {
        if (executor == null) {
            executor = ExecutorFactory.create(task, agentNode, userContext);
        }
        return executor;
    }
    
    /**
     * 验证节点完整性
     */
    public boolean isValid() {
        return task != null 
            && executionStrategy != null 
            && agentNode != null
            && task.getAgentId() != null;
    }
}
```

### 方案二：改进 GraphBuilder

修改 `GraphBuilder` 使用 `GraphNode`：

```java
public class GraphBuilder {
    
    /**
     * 构建图节点列表
     * 将 Task、ExecutionStrategy、Agent 组合成 GraphNode
     */
    private static List<GraphNode> buildGraphNodes(
            TaskPlan taskPlan, 
            Map<Long, ServiceNode> workersByID) {
        
        List<GraphNode> nodes = new ArrayList<>();
        
        for (Task task : taskPlan.getTasks()) {
            ServiceNode agentNode = workersByID.get(task.getAgentId());
            
            if (agentNode == null) {
                log.warn("未找到 Agent 节点: taskId={}, agentId={}", 
                    task.getId(), task.getAgentId());
                continue;
            }
            
            ExecutionStrategy strategy = task.getExecutionStrategy();
            if (strategy == null) {
                strategy = ExecutionStrategy.SILENT;
                log.warn("任务未指定执行策略，使用默认 SILENT: taskId={}", 
                    task.getId());
            }
            
            GraphNode graphNode = GraphNode.builder()
                .task(task)
                .executionStrategy(strategy)
                .agentNode(agentNode)
                .build();
            
            if (graphNode.isValid()) {
                nodes.add(graphNode);
            } else {
                log.error("图节点验证失败: taskId={}", task.getId());
            }
        }
        
        return nodes;
    }
    
    /**
     * 根据 GraphNode 列表构建执行图
     */
    public static CompiledGraph<WorkspaceState> build(
            TaskPlan taskPlan,
            Map<Long, ServiceNode> workersByID,
            UserContext userContext) throws GraphStateException {
        
        // 1. 构建图节点列表
        List<GraphNode> graphNodes = buildGraphNodes(taskPlan, workersByID);
        
        if (graphNodes.isEmpty()) {
            throw new GraphStateException("没有有效的图节点");
        }
        
        // 2. 构建图
        StateGraph<WorkspaceState> graph = new StateGraph<>(WorkspaceState::new);
        
        // 3. 添加节点
        for (GraphNode graphNode : graphNodes) {
            TaskExecutor executor = graphNode.getExecutor(userContext);
            
            graph.addNode(graphNode.getId(), state -> {
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("执行图节点: nodeId={}, task={}, agent={}, strategy={}", 
                            graphNode.getId(),
                            graphNode.getTask().getTitle(),
                            graphNode.getAgentNode().getClientName(),
                            graphNode.getExecutionStrategy());
                        return executor.apply(state);
                    } catch (Exception e) {
                        log.error("节点执行异常: nodeId={}", graphNode.getId(), e);
                        throw new RuntimeException("节点执行失败: " + graphNode.getId(), e);
                    }
                }, executorService);
            });
        }
        
        // 4. 构建边（依赖关系）
        buildEdges(graph, taskPlan);
        
        return graph.compile();
    }
    
    /**
     * 构建图的边（依赖关系）
     */
    private static void buildEdges(
            StateGraph<WorkspaceState> graph, 
            TaskPlan taskPlan) throws GraphStateException {
        
        // ... 现有的边构建逻辑 ...
    }
}
```

### 方案三：节点元数据增强

为图节点添加更多元数据，便于监控和调试：

```java
@Data
@Builder
public class GraphNode {
    // ... 基础字段 ...
    
    /**
     * 节点元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 节点创建时间
     */
    @Builder.Default
    private long createdAt = System.currentTimeMillis();
    
    /**
     * 节点执行次数
     */
    @Builder.Default
    private AtomicInteger executionCount = new AtomicInteger(0);
    
    /**
     * 节点执行耗时（毫秒）
     */
    private volatile long executionTime;
    
    /**
     * 获取节点摘要信息
     */
    public String getSummary() {
        return String.format(
            "GraphNode[id=%s, task=%s, agent=%s, strategy=%s, executions=%d]",
            getId(),
            task != null ? task.getTitle() : "null",
            agentNode != null ? agentNode.getClientName() : "null",
            executionStrategy,
            executionCount.get()
        );
    }
}
```

## 优势对比

### 当前架构
- ❌ 信息分散，查找效率低
- ❌ 难以追踪节点完整信息
- ❌ 扩展性差
- ✅ 简单直接

### 改进后架构
- ✅ 信息集中，易于管理
- ✅ 一次查找，性能更好
- ✅ 易于扩展新功能
- ✅ 更好的可观测性
- ✅ 便于单元测试

## 实施建议

### 阶段一：创建 GraphNode 类
1. 创建 `GraphNode.java`
2. 添加基础字段和方法
3. 添加单元测试

### 阶段二：重构 GraphBuilder
1. 添加 `buildGraphNodes()` 方法
2. 修改 `build()` 方法使用 GraphNode
3. 保持向后兼容

### 阶段三：增强功能
1. 添加节点元数据
2. 添加节点监控
3. 添加节点缓存

## 代码示例：完整实现

```java
// GraphNode.java
package com.example.ddd.domain.agent.service.execute.graph;

import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.executor.ExecutionStrategy;
import com.example.ddd.domain.agent.service.execute.executor.ExecutorFactory;
import com.example.ddd.domain.agent.service.execute.executor.TaskExecutor;
import com.example.ddd.domain.agent.service.execute.task.Task;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Data
@Builder
public class GraphNode {
    private Task task;
    private ExecutionStrategy executionStrategy;
    private ServiceNode agentNode;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @Builder.Default
    private AtomicInteger executionCount = new AtomicInteger(0);
    
    private transient TaskExecutor executor; // 不序列化
    
    public String getId() {
        return task != null ? task.getId() : null;
    }
    
    public Long getAgentId() {
        return task != null ? task.getAgentId() : null;
    }
    
    public TaskExecutor getExecutor(UserContext userContext) {
        if (executor == null) {
            executor = ExecutorFactory.create(task, agentNode, userContext);
        }
        return executor;
    }
    
    public boolean isValid() {
        return task != null 
            && executionStrategy != null 
            && agentNode != null
            && task.getAgentId() != null;
    }
    
    public String getSummary() {
        return String.format(
            "GraphNode[id=%s, task=%s, agent=%s, strategy=%s]",
            getId(),
            task != null ? task.getTitle() : "null",
            agentNode != null ? agentNode.getClientName() : "null",
            executionStrategy
        );
    }
    
    public void incrementExecutionCount() {
        executionCount.incrementAndGet();
    }
}
```

## 总结

将执行策略、Task 和 Agent 组合成 `GraphNode` 类有以下好处：

1. **更好的封装**：相关信息集中管理
2. **更高的性能**：减少查找操作
3. **更强的可扩展性**：易于添加新功能
4. **更好的可观测性**：便于监控和调试
5. **更清晰的代码**：逻辑更清晰，易于维护

建议采用**方案一 + 方案二**的组合，既保持了代码的清晰性，又提供了良好的扩展性。

