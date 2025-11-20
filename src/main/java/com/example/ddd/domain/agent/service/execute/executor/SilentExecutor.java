package com.example.ddd.domain.agent.service.execute.executor;

import com.example.ddd.domain.agent.service.armory.AiService;
import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.context.EventType;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.graph.WorkspaceState;
import com.example.ddd.domain.agent.service.execute.task.Task;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.internal.Json;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 静默执行器
 * - 只将结果保存到state中
 * - 不向用户输出内容，但发送任务状态事件
 * - 适用于：数据预处理、中间计算、后台任务
 */
@Slf4j
public class SilentExecutor extends BaseTaskExecutor {

    public SilentExecutor(Task task, ServiceNode serviceNode, UserContext userContext) {
        super(task, serviceNode, userContext);
    }

    @Override
    public Map<String, Object> apply(WorkspaceState state) {
        log.info("静默执行器执行任务: taskId={}, title={}", task.getId(), task.getTitle());
        // 获取用户上下文
        UserContext userContext = getUserContext();
        userContext.emit(UserContext.TaskStatusEvent.builder()
                .type(EventType.TASK_START)
                .taskId(task.getId())
                .message("任务开始执行")
                .build());
        Map<String, Object> taskInput = getTaskInput(state);
        AiService aiService = serviceNode.getAiService();
        if (aiService == null) {
            log.error("ServiceNode的AiService为空: taskId={}", task.getId());
            // 发送失败事件
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type(EventType.TASK_FAILED)
                        .taskId(task.getId())
                        .message("AiService未初始化")
                        .error("AiService为空")
                        .build());
            }
            throw new IllegalStateException("ServiceNode的AiService为空: taskId=" + task.getId());
        }

        try {

            TokenStream tokenStream = aiService.chat(Json.toJson(taskInput));
            StringBuilder resultBuilder = new StringBuilder();
            CountDownLatch latch = new CountDownLatch(1);
            tokenStream.onPartialResponse(token -> {

            });
            tokenStream.onCompleteResponse(e -> {
                AiMessage aiMessage = e.aiMessage();
                if (aiMessage != null && aiMessage.text() != null) {
                    resultBuilder.append(aiMessage.text());
                }
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type(EventType.TASK_COMPLETE)
                            .taskId(task.getId())
                            .message("任务执行完成")
                            .build());
                }
                latch.countDown();
            });

            tokenStream.onError(error -> {
                log.error("静默执行器执行失败: taskId={}, error={}", task.getId(), error.getMessage());

                // 发送任务失败事件
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type(EventType.TASK_FAILED)
                            .taskId(task.getId())
                            .message("任务执行失败")
                            .error(error.getMessage())
                            .build());
                }

                latch.countDown();
            });
            tokenStream.start();
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("等待token流完成被中断: taskId={}", task.getId());
                // 发送中断事件
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type(EventType.TASK_FAILED)
                            .taskId(task.getId())
                            .message("任务执行被中断")
                            .error("等待token流完成被中断")
                            .build());
                }
                throw new RuntimeException("等待token流完成被中断: taskId=" + task.getId(), e);
            }
            String result = resultBuilder.toString();
            Map<String, Object> stateUpdate = saveTaskResult(state, result);
            log.info("静默执行器完成: taskId={}, resultLength={}", task.getId(), result.length());
            stateUpdate.put("currentAgent", serviceNode.getRole());
            stateUpdate.put("taskId", task.getId());
            return stateUpdate;
        } catch (Exception e) {
            log.error("静默执行器运行时异常: taskId={}, error={}", task.getId(), e.getMessage(), e);
            userContext.emit(UserContext.TaskStatusEvent.builder()
                    .type("task_failed")
                    .taskId(task.getId())
                    .message("任务执行失败")
                    .error(e.getMessage())
                    .build());
            throw e;
        }
    }
}

