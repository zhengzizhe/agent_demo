package com.example.ddd.domain.agent.service.execute.graph;

import com.example.ddd.domain.agent.service.armory.ServiceNode;

import com.example.ddd.domain.agent.service.execute.Orchestrator;
import com.example.ddd.domain.agent.service.execute.executor.ExecutorFactory;
import com.example.ddd.domain.agent.service.execute.executor.TaskExecutor;
import com.example.ddd.domain.agent.service.execute.task.Task;
import com.example.ddd.domain.agent.service.execute.task.TaskPlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.StateGraph;

import java.util.*;
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
        if (taskPlan == null || taskPlan.getTasks() == null || taskPlan.getTasks().isEmpty()) {
            throw new IllegalArgumentException("TaskPlan 不能为空");
        }
        if (orchestrator == null) {
            throw new IllegalArgumentException("Orchestrator 不能为空");
        }
        Map<Long, ServiceNode> workersByID = orchestrator.getWorkersByID();
        return build(taskPlan, workersByID);
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

        if (taskPlan == null || taskPlan.getTasks() == null || taskPlan.getTasks().isEmpty()) {
            throw new IllegalArgumentException("TaskPlan 不能为空");
        }

        StateGraph<WorkspaceState> graph = new StateGraph<>(WorkspaceState::new);

        // 1. 添加所有节点
        for (Task task : taskPlan.getTasks()) {
            TaskExecutor executor = ExecutorFactory.create(task, workersByID.get(task.getAgentId()));
            graph.addNode(task.getId(), node_async(executor));
        }
        Map<String, Task.TaskInputs> inputMap =
                taskPlan.getTasks().stream().collect(Collectors.toMap(Task::getId, Task::getInputs));
        Set<String> allNodes = inputMap.keySet();
        Set<String> allTargets = new HashSet<>();
        for (Map.Entry<String, Task.TaskInputs> entry : inputMap.entrySet()) {
            String taskId = entry.getKey();
            Task.TaskInputs inputs = entry.getValue();
            if (inputs != null && StringUtils.isNotBlank(inputs.getFromTask())) {
                String fromTask = inputs.getFromTask();
                graph.addEdge(fromTask, taskId);
                allTargets.add(taskId);
            }
        }

        // 3. 自动推导入口节点（即没有被任何任务指向的任务）
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
        // 找出所有叶子节点（没有出边的节点）
        Set<String> allSources = new HashSet<>(); // 所有作为 fromTask 的节点ID
        for (Task task : taskPlan.getTasks()) {
            if (task.getInputs() != null && StringUtils.isNotBlank(task.getInputs().getFromTask())) {
                allSources.add(task.getInputs().getFromTask());
            }
        }

// 叶子节点 = 所有节点 - 作为 fromTask 的节点
        List<String> leafNodes = allNodes.stream()
                .filter(node -> !allSources.contains(node))
                .toList();

// 为所有叶子节点添加指向 END 的边
        for (String leaf : leafNodes) {
            graph.addEdge(leaf, StateGraph.END);
        }
        return graph.compile();
    }


}

