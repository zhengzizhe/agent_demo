package com.example.ddd.application.agent.service.execute.graph;
import com.example.ddd.application.agent.service.Orchestrator;
import com.example.ddd.application.agent.service.armory.ServiceNode;
import com.example.ddd.application.agent.service.execute.context.UserContext;
import com.example.ddd.application.agent.service.execute.executor.ExecutorFactory;
import com.example.ddd.application.agent.service.execute.executor.TaskExecutor;
import com.example.ddd.application.agent.service.execute.task.Task;
import com.example.ddd.application.agent.service.execute.task.TaskPlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 图构建器
 * 根据 TaskPlan 和 Orchestrator 构建执行图
 */
@Slf4j
public class GraphBuilder {
    // 线程池用于异步执行节点
    private static final ExecutorService executorService = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "GraphNodeExecutor-" + System.currentTimeMillis());
        return t;
    });

    /**
     * 根据 TaskPlan 和 Orchestrator 构建执行图
     *
     * @param taskPlan     任务计划
     * @param orchestrator 编排器（包含 supervisor 和 worker 节点）
     * @return 编译后的图
     */
    public static CompiledGraph<WorkspaceState> build(TaskPlan taskPlan, Orchestrator orchestrator) throws GraphStateException {
        return build(taskPlan, orchestrator, null);
    }

    /**
     * 根据 TaskPlan 和 Orchestrator 构建执行图（带UserContext）
     *
     * @param taskPlan     任务计划
     * @param orchestrator 编排器（包含 supervisor 和 worker 节点）
     * @param userContext  用户上下文
     * @return 编译后的图
     */
    public static CompiledGraph<WorkspaceState> build(TaskPlan taskPlan, Orchestrator orchestrator, UserContext userContext) throws GraphStateException {
        if (taskPlan == null) {
            throw new IllegalArgumentException("TaskPlan 不能为空");
        }
        if (taskPlan.getTasks() == null || taskPlan.getTasks().isEmpty()) {
            return null;
        }
        if (orchestrator == null) {
            throw new IllegalArgumentException("Orchestrator 不能为空");
        }

        Map<String, ServiceNode> workersByID = orchestrator.getWorkersByID();
        return build(taskPlan, workersByID, userContext);
    }

    /**
     * 根据 TaskPlan 和 ServiceNode 列表构建执行图
     *
     * @param taskPlan 任务计划
     * @return 编译后的图
     */
    public static CompiledGraph<WorkspaceState> build(TaskPlan taskPlan,
                                                      Map<String, ServiceNode> workersByID)
            throws GraphStateException {
        return build(taskPlan, workersByID, null);
    }

    /**
     * 根据 TaskPlan 和 ServiceNode 列表构建执行图（带UserContext）
     *
     * @param taskPlan    任务计划
     * @param workersByID ServiceNode映射
     * @param userContext 用户上下文
     * @return 编译后的图
     */
    public static CompiledGraph<WorkspaceState> build(TaskPlan taskPlan,
                                                      Map<String, ServiceNode> workersByID,
                                                      UserContext userContext)
            throws GraphStateException {

        if (taskPlan == null || taskPlan.getTasks() == null || taskPlan.getTasks().isEmpty()) {
            throw new IllegalArgumentException("TaskPlan 不能为空");
        }

        StateGraph<WorkspaceState> graph = new StateGraph<>(WorkspaceState::new);
        for (Task task : taskPlan.getTasks()) {
            TaskExecutor executor = ExecutorFactory.create(task, workersByID.get(task.getAgentId()), userContext);
            // 创建真正异步的 NodeAction
            graph.addNode(task.getId(), state -> {
                // 在独立线程中执行，实现真正的异步
                return CompletableFuture.supplyAsync(() -> {
                    try {
                        log.debug("异步执行节点: taskId={}, thread={}", task.getId(), Thread.currentThread().getName());
                        return executor.apply(state);
                    } catch (Exception e) {
                        log.error("节点执行异常: taskId={}", task.getId(), e);
                        throw new RuntimeException("节点执行失败: taskId=" + task.getId(), e);
                    }
                }, executorService);
            });
        }
        Map<String, Task.TaskInputs> inputMap =
                taskPlan.getTasks().stream().collect(Collectors.toMap(Task::getId, Task::getInputs));
        Set<String> allNodes = inputMap.keySet();
        Set<String> allTargets = new HashSet<>();
        for (Map.Entry<String, Task.TaskInputs> entry : inputMap.entrySet()) {
            String taskId = entry.getKey();
            Task.TaskInputs inputs = entry.getValue();
            if (inputs != null && inputs.getFromTask() != null && !inputs.getFromTask().isEmpty()) {
                List<String> fromTasks = inputs.getFromTask();
                for (String fromTask : fromTasks) {
                    if (StringUtils.isNotBlank(fromTask)) {
                        graph.addEdge(fromTask, taskId);
                        allTargets.add(taskId);
                    }
                }
            }
        }

        List<String> rootNodes = allNodes.stream()
                .filter(node -> !allTargets.contains(node))
                .toList();

        if (rootNodes.isEmpty()) {
            throw new GraphStateException("任务图中没有找到入口任务（无前置依赖的任务）");
        }

        if (rootNodes.size() > 1) {
            System.out.println("⚠ 多个入口任务，将全部从 START 指向它们：" + rootNodes);
        }
        for (String root : rootNodes) {
            graph.addEdge(StateGraph.START, root);
        }
        Set<String> nodesWithTargets = new HashSet<>();
        for (Task task : taskPlan.getTasks()) {
            if (task.getInputs() != null && task.getInputs().getFromTask() != null && !task.getInputs().getFromTask().isEmpty()) {
                nodesWithTargets.addAll(task.getInputs().getFromTask());
            }
        }
        List<String> leafNodes = allNodes.stream()
                .filter(node -> !nodesWithTargets.contains(node))
                .toList();
        for (String leaf : leafNodes) {
            graph.addEdge(leaf, StateGraph.END);
        }
        return graph.compile();
    }


}

