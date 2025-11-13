package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.JSON;
import com.example.ddd.common.utils.ToonUtil;
import com.example.ddd.domain.agent.service.execute.CriticService.CriticService;
import com.example.ddd.domain.agent.service.execute.ExecutorService.ExecutorService;
import com.example.ddd.domain.agent.service.execute.ResearcherService.ResearcherService;
import com.example.ddd.domain.agent.service.execute.SupervisorService.SupervisorService;
import com.example.ddd.domain.agent.service.execute.blackBoard.Blackboard;
import com.example.ddd.domain.agent.service.execute.plan.ExecutionMode;
import com.example.ddd.domain.agent.service.execute.plan.PlanParser;
import com.example.ddd.domain.agent.service.execute.plan.SupervisorPlan;
import com.example.ddd.domain.agent.service.execute.task.Task;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class AgentOrchestrator {
    private final SupervisorService supervisor;
    private final ResearcherService researcher;
    private final ExecutorService executor;
    private final CriticService critic;
    private final PlanParser planParser;
    public AgentOrchestrator(SupervisorService supervisor,
                             ResearcherService researcher,
                             ExecutorService executor,
                             CriticService critic
    ) {
        this.supervisor = supervisor;
        this.researcher = researcher;
        this.executor = executor;
        this.critic = critic;
        this.planParser = new PlanParser();
    }
    public void runStream(Long agentId, String goal, int maxRounds,
                          Blackboard board, FluxSink<String> emitter) {
        log.info("开始执行: agentId={}, goal={}", agentId, goal);
        try {
            String planPrompt = buildPlanPrompt(goal, board);
            log.info("Supervisor规划开始: agentId={}, promptLength={}", agentId, planPrompt.length());
            TokenStream planStream = supervisor.planAndRoute(planPrompt);
            String planJson = planParser.extractCompleteResponse(planStream, "Supervisor规划");
            log.info("Supervisor规划完成: agentId={}", agentId);
            SupervisorPlan plan = JSON.parseObject(planJson, SupervisorPlan.class);
            if (plan.getGreeting() != null && !plan.getGreeting().isBlank()) {
                String greeting = ToonUtil.formatAgentThought(plan.getGreeting());
                emitter.next(greeting + "\n\n");
            }

            if (!Boolean.TRUE.equals(plan.getShouldProceed())) {
                emitter.complete();
                return;
            }
            List<Task> tasks = plan.getTasks();
            if (tasks.isEmpty()) {
                emitter.complete();
                return;
            }
            if (isSimplePlan(tasks)) {
                log.info("检测到简单任务计划，使用单Agent模式执行第一个任务");
                executeSimpleTask(tasks.get(0), board, emitter);
                return;
            }
            log.info("检测到复杂任务计划，使用多Agent编排模式");
            executeComplexTasks(goal, tasks, board, emitter);
        } catch (Exception e) {
            log.error("任务执行失败", e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }
    /**
     * 判断是否为简单任务计划
     * 简单任务：只有一个任务，或者都是research类型的简单查询
     */
    private boolean isSimplePlan(List<Task> tasks) {
        if (tasks.size() != 1) {
            return false; // 多个任务一定是复杂任务
        }
        Task task = tasks.get(0);
        // 如果只有一个research任务，且payload不复杂，则认为是简单任务
        return "Researcher".equals(task.getAssignee()) &&
                task.getPayload() != null &&
                task.getPayload().length() < 200; // payload长度限制
    }

    private void executeSimpleTask(Task task, Blackboard board, FluxSink<String> emitter) {
        try {
            TokenStream stream;
            if ("Researcher".equals(task.getAssignee())) {
                stream = researcher.research(task.getPayload());
            } else if ("Executor".equals(task.getAssignee())) {
                stream = executor.execute(task.getPayload());
            } else {
                throw new IllegalArgumentException("Unknown assignee: " + task.getAssignee());
            }

            stream.onPartialResponse(partialResponse -> {
                if (!emitter.isCancelled()) {
                    String formattedResponse = ToonUtil.formatAgentResponse(partialResponse);
                    emitter.next(formattedResponse);
                }
            });

            stream.onCompleteResponse(completeResponse -> {
                if (completeResponse.tokenUsage() != null) {
                    log.info("简单任务Token消耗 [{}] - inputTokens={}, outputTokens={}, totalTokens={}",
                            task.getAssignee(),
                            completeResponse.tokenUsage().inputTokenCount(),
                            completeResponse.tokenUsage().outputTokenCount(),
                            completeResponse.tokenUsage().totalTokenCount());
                } else {
                    log.warn("简单任务Token消耗信息不可用 [{}]", task.getAssignee());
                }
                if (!emitter.isCancelled()) {
                    String doneMessage = ToonUtil.formatAgentResult("[DONE]");
                    emitter.next("\n\n" + doneMessage);
                    emitter.complete();
                }
            });

            stream.onError(error -> {
                log.error("简单任务执行失败", error);
                if (!emitter.isCancelled()) {
                    emitter.error(error);
                }
            });

            stream.start();

        } catch (Exception e) {
            log.error("简单任务执行异常", e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }

    private void executeComplexTasks(String goal, List<Task> tasks, Blackboard board, FluxSink<String> emitter) {
        try {
            ExecutionMode executionMode = determineExecutionMode(tasks);
            log.info("执行{}个任务，模式: {}", tasks.size(), executionMode);
            Map<String, String> taskResults = new HashMap<>();
            if (executionMode == ExecutionMode.PARALLEL) {
                taskResults = executeTasksParallel(tasks, board);
            } else {
                taskResults = executeTasksSerial(tasks, board);
            }
            String summarizePrompt = buildSummarizePrompt(goal, taskResults, tasks);
            streamFinalResult(summarizePrompt, emitter);
        } catch (Exception e) {
            log.error("复杂任务执行失败", e);
            if (!emitter.isCancelled()) {
                emitter.error(e);
            }
        }
    }


    private ExecutionMode determineExecutionMode(List<Task> tasks) {
        if (tasks.size() <= 2) {
            return ExecutionMode.SERIAL; // 小任务串行节省token
        } else if (tasks.size() <= 5) {
            return ExecutionMode.PARALLEL; // 中等任务并行提高效率
        } else {
            return ExecutionMode.SERIAL; // 大任务串行避免token爆炸
        }
    }

    private String buildPlanPrompt(String goal, Blackboard board) {
        String toolCatalog = buildToolCatalog();
        Map<String, String> snapshot = board.snapshot();
        String boardInfo = "";

        if (!snapshot.isEmpty()) {
            StringBuilder boardBuilder = new StringBuilder();
            int totalLength = 0;
            for (Map.Entry<String, String> entry : snapshot.entrySet()) {
                String item = entry.getKey() + "=" + entry.getValue().substring(0, Math.min(50, entry.getValue().length()));
                if (totalLength + item.length() > 200) { // 限制总长度
                    boardBuilder.append("...(省略)").append(snapshot.size() - boardBuilder.toString().split(",").length).append("条)");
                    break;
                }
                if (boardBuilder.length() > 0) boardBuilder.append(", ");
                boardBuilder.append(item);
                totalLength += item.length();
            }
            boardInfo = "\n历史: " + boardBuilder.toString();
        }

        return String.format(
                "目标: %s%s\n\n工具: %s\n\n严格返回JSON不许其他内容: {\"shouldProceed\":boolean,\"executionMode\":\"PARALLEL|SERIAL\",\"greeting\":\"\",\"tasks\":[{\"id\":\"t1\",\"type\":\"research|execute\",\"assignee\":\"Researcher|Executor\",\"payload\":\"\"}]}",
                goal, boardInfo, toolCatalog
        );
    }

    private String buildSummarizePrompt(String goal, Map<String, String> taskResults, List<Task> tasks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("目标: ").append(goal).append("\n\n任务结果:\n");
        int maxTotalLength = 1000; // 从1500减少到1000
        int remainingLength = maxTotalLength - prompt.length();
        int maxPerTask = tasks.size() > 0 ? Math.max(100, remainingLength / tasks.size()) : remainingLength; // 每任务从200减少到100
        for (Task task : tasks) {
            String result = taskResults.get(task.getId());
            if (result == null) {
                result = "失败";
            } else if (result.length() > maxPerTask) {
                result = result.substring(0, maxPerTask) + "...";
            }
            prompt.append(String.format("%s: %s\n", task.getId(), result));
            if (prompt.length() >= maxTotalLength) {
                prompt.append("(省略...)");
                break;
            }
        }
        prompt.append("\n请给出简洁的最终答案。");
        return prompt.toString();
    }

    private String buildToolCatalog() {
        return "工具:\n" +
                "  - research(question):检索整合信息\n" +
                "  - execute(spec):执行操作";
    }

    private Map<String, String> executeTasksParallel(List<Task> tasks, Blackboard board) {
        Map<String, String> results = new ConcurrentHashMap<>();
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Task task : tasks) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String result = executeTask(task, board);
                    results.put(task.getId(), result);
                    board.put(task.getId(), result);
                } catch (Exception e) {
                    log.error("任务失败: taskId={}", task.getId(), e);
                    String errorMsg = "失败: " + e.getMessage();
                    results.put(task.getId(), errorMsg);
                    board.put(task.getId(), errorMsg);
                }
            });
            futures.add(future);
        }
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.error("执行超时", e);
        } catch (Exception e) {
            log.error("并行执行失败", e);
        }
        return results;
    }

    private Map<String, String> executeTasksSerial(
            List<Task> tasks, Blackboard board) {
        Map<String, String> results = new HashMap<>();
        for (Task task : tasks) {
            try {
                String result = executeTask(task, board);
                results.put(task.getId(), result);
                board.put(task.getId(), result);
            } catch (Exception e) {
                log.error("任务失败: taskId={}", task.getId(), e);
                String errorMsg = "失败: " + e.getMessage();
                results.put(task.getId(), errorMsg);
                board.put(task.getId(), errorMsg);
            }
        }
        return results;
    }

    private String executeTask(Task task, Blackboard board) {
        log.info("开始执行任务: taskId={}, assignee={}, payload={}", task.getId(), task.getAssignee(), task.getPayload());
        TokenStream stream;
        if ("Researcher".equals(task.getAssignee())) {
            stream = researcher.research(task.getPayload());
        } else if ("Executor".equals(task.getAssignee())) {
            stream = executor.execute(task.getPayload());
        } else {
            throw new IllegalArgumentException("Unknown assignee: " + task.getAssignee());
        }

        String result = planParser.extractCompleteResponse(stream, String.format("Task[%s-%s]", task.getId(), task.getAssignee()));
        log.info("任务执行完成: taskId={}, assignee={}", task.getId(), task.getAssignee());
        return result;
    }

    private void streamFinalResult(String prompt, FluxSink<String> emitter) {
        TokenStream tokenStream = supervisor.planAndRoute(prompt);
        tokenStream.onPartialResponse(partialResponse -> {
            if (!emitter.isCancelled()) {
                String formattedResponse = ToonUtil.formatAgentResponse(partialResponse);
                emitter.next(formattedResponse);
            }
        });
        tokenStream.onCompleteResponse(completeResponse -> {
            // 记录token消耗
            if (completeResponse.tokenUsage() != null) {
                log.info("汇总结果Token消耗 - inputTokens={}, outputTokens={}, totalTokens={}",
                        completeResponse.tokenUsage().inputTokenCount(),
                        completeResponse.tokenUsage().outputTokenCount(),
                        completeResponse.tokenUsage().totalTokenCount());
            } else {
                log.warn("汇总结果Token消耗信息不可用");
            }
            if (!emitter.isCancelled()) {
                String doneMessage = ToonUtil.formatAgentResult("[DONE]");
                emitter.next("\n\n" + doneMessage);
                emitter.complete();
            }
        });
        tokenStream.onError(error -> {
            log.error("汇总失败", error);
            if (!emitter.isCancelled()) {
                emitter.error(error);
            }
        });
        tokenStream.start();
    }
}
