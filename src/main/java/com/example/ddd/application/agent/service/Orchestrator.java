package com.example.ddd.application.agent.service;

import com.example.ddd.application.agent.service.execute.memory.InMemory;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.application.agent.service.armory.AiService;
import com.example.ddd.application.agent.service.armory.ServiceNode;
import com.example.ddd.application.agent.service.execute.context.EventType;
import com.example.ddd.application.agent.service.execute.context.UserContext;
import com.example.ddd.application.agent.service.execute.graph.GraphBuilder;
import com.example.ddd.application.agent.service.execute.graph.WorkspaceState;
import com.example.ddd.application.agent.service.execute.role.AgentRole;
import com.example.ddd.application.agent.service.execute.task.TaskPlan;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.service.TokenStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphStateException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * Orchestrator：
 * - 持有一个「主管 AiService + 主管的 ServiceNode 配置」
 * - 持有一个「子 Agent ServiceNode 列表」
 * - 主管先生成 TaskPlan（任务计划）
 * - Orchestrator 根据 plan 构建 StateGraph 并执行
 */
@Slf4j
@Getter
@Setter
public class Orchestrator {
    private final ServiceNode supervisorNode;      // 主管节点配置（但不会放进执行图）
    private final List<ServiceNode> workerNodes;   // 子 Agent 节点列表
    private final Map<String, ServiceNode> workersByID;
    private String orchestratorId;

    public Orchestrator(String orchestratorId, ServiceNode supervisorNode, List<ServiceNode> workerNodes) {
        if (supervisorNode.getRole() != AgentRole.SUPERVISOR) {
            throw new IllegalArgumentException("supervisorNode.role must be SUPERVISOR");
        }
        this.orchestratorId = orchestratorId;
        this.supervisorNode = supervisorNode;
        this.workerNodes = workerNodes;
        this.workersByID = workerNodes.stream()
                .collect(Collectors.toMap(
                        ServiceNode::getId,
                        n -> n
                ));
    }

    /**
     * Step 1: 主管生成任务计划
     *
     * @param userRequest 用户请求
     * @param userContext 用户上下文（用于发送事件）
     * @return 任务计划
     */
    public TaskPlan plan(String userRequest, UserContext userContext) {
        log.info("多agent执行中 Supervisor开始生成任务计划: userRequest={}", userRequest);
        if (userContext != null) {
            userContext.emit(UserContext.TaskStatusEvent.builder()
                    .type(EventType.PLANNING_START)
                    .message("Supervisor开始生成任务计划")
                    .build());
        }
        AiService supAi = supervisorNode.getAiService();
        if (supAi == null) {
            throw new IllegalStateException("Supervisor AiService 未初始化");
        }

        // 获取历史对话记忆
        String historyContext = "";
        if (userContext != null && userContext.getUserId() != null && userContext.getSessionId() != null) {
            historyContext = InMemory.getInstance()
                    .formatHistoryForPrompt(userContext.getUserId(), userContext.getSessionId(), 10);
        }

        String planningPrompt = """
                {
                  "userRequest": "%s",
                  "subAgents": %s,
                  "chatHistory(作为记忆联系上下午，不要从记忆中汇总问题)":%s
                }
                """.formatted(userRequest, subWorkersDesc(), historyContext);
        TokenStream chat = supAi.chat(planningPrompt);
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder builder = new StringBuilder();
        chat.onPartialResponse(token -> {

        });

        chat.onCompleteResponse(e -> {
            AiMessage aiMessage = e.aiMessage();
            builder.append(aiMessage.text());
            latch.countDown();
        });
        chat.onError(error -> {
            log.error("多agent执行中 Supervisor生成计划失败: error={}", error.getMessage(), error);
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type(EventType.PLANNING_FAILED)
                        .message("Supervisor任务计划生成失败")
                        .error(error.getMessage())
                        .build());
            }
            throw new RuntimeException(error);
        });
        chat.start();
        try {
            latch.await();
            String response = builder.toString();
            log.info("多agent执行中 Supervisor返回的计划: response={}", response);
            TaskPlan taskPlan = JSON.parseObject(response, TaskPlan.class);
            if (taskPlan == null) {
                throw new IllegalStateException("Supervisor生成的任务计划为空或无效");
            }
            log.info("多agent执行中 任务计划生成成功: summary={}, totalTasks={}",
                    taskPlan.getSummary(), taskPlan.getTotalTasks());
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type(EventType.PLAN_READY)
                        .message(taskPlan.getSummary())
                        .content(JSON.toJSON(taskPlan))
                        .build());
            }
            return taskPlan;
        } catch (Exception e) {
            log.error("多agent执行中 解析任务计划失败: error={}", e.getMessage(), e);
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type(EventType.PLAN_PARSE_FAILED)
                        .message("解析任务计划失败")
                        .error(e.getMessage())
                        .build());
            }
            throw new RuntimeException("解析任务计划失败: " + e.getMessage(), e);
        }
    }


    private Object subWorkersDesc() {
        return this.getWorkerNodes().stream().map(i -> {
            HashMap<String, Object> stringStringHashMap = new HashMap<>();
            stringStringHashMap.put("id", i.getId());
            stringStringHashMap.put("desc", i.getClientDescription());
            return stringStringHashMap;
        }).toList();
    }


    /**
     * Step 2: 根据 TaskPlan 构建 StateGraph
     * 使用 GraphBuilder 构建图，执行器作为节点
     */
    public CompiledGraph<WorkspaceState> buildGraph(TaskPlan taskPlan, UserContext userContext) throws GraphStateException {
        log.info("多agent执行中 开始构建执行图: totalTasks={}", taskPlan.getTotalTasks());
        return GraphBuilder.build(taskPlan, this, userContext);
    }

    /**
     * Step 3: 执行完整流程
     * 1. 主管生成任务计划
     * 2. 构建执行图
     * 3. 执行图
     *
     * @param userRequest 用户请求
     * @param userContext 用户上下文（用于流式输出）
     * @return 执行结果流
     */
    public void execute(String userRequest, UserContext userContext) throws GraphStateException {
        try {
            log.info("开始执行任务流程: {}", userRequest);
            TaskPlan taskPlan = plan(userRequest, userContext);
            log.info("任务计划生成完成: {}", taskPlan.getSummary());
            CompiledGraph<WorkspaceState> graph = buildGraph(taskPlan, userContext);
            if (graph != null) {
                log.info("执行图构建完成");
                Map<String, Object> init = new HashMap<>();
                init.put("userMessage", userRequest);

                // 添加历史对话到 state
                if (userContext != null && userContext.getUserId() != null && userContext.getSessionId() != null) {
                    String historyContext = InMemory.getInstance()
                            .formatHistoryForPrompt(userContext.getUserId(), userContext.getSessionId(), 10);
                    if (!historyContext.isEmpty()) {
                        init.put("historyContext", historyContext);
                    }
                }

                log.info("开始执行图");
                graph.invoke(init);
            }
        } catch (Exception e) {
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type(EventType.EXECUTION_FAILED)
                        .message("流程异常")
                        .error(e.getMessage())
                        .build());
            }
            log.error("执行任务流程失败: {}", e.getMessage(), e);
        }
    }

}
