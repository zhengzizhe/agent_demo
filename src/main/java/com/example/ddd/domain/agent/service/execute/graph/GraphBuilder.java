package com.example.ddd.domain.agent.service.execute.graph;

import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.Orchestrator;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.executor.ExecutorFactory;
import com.example.ddd.domain.agent.service.execute.executor.TaskExecutor;
import com.example.ddd.domain.agent.service.execute.task.Task;
import com.example.ddd.domain.agent.service.execute.task.TaskPlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.bsc.langgraph4j.action.AsyncNodeAction.node_async;

/**
 * 图构建器
 * 根据 TaskPlan 和 Orchestrator 构建执行图
 */
@Slf4j
public class GraphBuilder {
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

        Map<Long, ServiceNode> workersByID = orchestrator.getWorkersByID();
        return build(taskPlan, workersByID, userContext);
    }

    /**
     * 根据 TaskPlan 和 ServiceNode 列表构建执行图
     *
     * @param taskPlan 任务计划
     * @return 编译后的图
     */
    public static CompiledGraph<WorkspaceState> build(TaskPlan taskPlan,
                                                      Map<Long, ServiceNode> workersByID)
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
                                                      Map<Long, ServiceNode> workersByID,
                                                      UserContext userContext)
            throws GraphStateException {

        if (taskPlan == null || taskPlan.getTasks() == null || taskPlan.getTasks().isEmpty()) {
            throw new IllegalArgumentException("TaskPlan 不能为空");
        }

        StateGraph<WorkspaceState> graph = new StateGraph<>(WorkspaceState::new);

        // 1. 添加所有节点，传递UserContext给执行器
        for (Task task : taskPlan.getTasks()) {
            TaskExecutor executor = ExecutorFactory.create(task, workersByID.get(task.getAgentId()), userContext);
            graph.addNode(task.getId(), node_async(executor));
        }
        Map<String, Task.TaskInputs> inputMap =
                taskPlan.getTasks().stream().collect(Collectors.toMap(Task::getId, Task::getInputs));
        Set<String> allNodes = inputMap.keySet();
        Set<String> allTargets = new HashSet<>();
        for (Map.Entry<String, Task.TaskInputs> entry : inputMap.entrySet()) {
            String taskId = entry.getKey();
            Task.TaskInputs inputs = entry.getValue();
            if (inputs != null && inputs.getFromTask() != null && !inputs.getFromTask().isEmpty()) {
                // 支持多个依赖任务，为每个依赖任务添加边
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
        Set<String> allSources = new HashSet<>();
        for (Task task : taskPlan.getTasks()) {
            if (task.getInputs() != null && task.getInputs().getFromTask() != null && !task.getInputs().getFromTask().isEmpty()) {
                allSources.addAll(task.getInputs().getFromTask());
            }
        }

        List<String> leafNodes = allNodes.stream()
                .filter(node -> !allSources.contains(node))
                .toList();
        for (String leaf : leafNodes) {
            graph.addEdge(leaf, StateGraph.END);
        }
        return graph.compile();
    }


}

