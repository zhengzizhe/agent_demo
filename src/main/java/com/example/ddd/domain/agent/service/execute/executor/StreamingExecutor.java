package com.example.ddd.domain.agent.service.execute.executor;

import com.example.ddd.domain.agent.service.armory.AiService;
import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.graph.WorkspaceState;
import com.example.ddd.domain.agent.service.execute.task.Task;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 流式输出执行器
 * - 将结果保存到state
 * - 同时通过UserContext向用户实时输出
 * - 适用于：生成报告、总结、说明等需要用户看到的内容
 */
@Slf4j
public class StreamingExecutor extends BaseTaskExecutor {
    public StreamingExecutor(Task task, ServiceNode serviceNode, UserContext userContext) {
        super(task, serviceNode, userContext);
    }

    @Override
    public Map<String, Object> apply(WorkspaceState state) {
        log.info("流式输出执行器执行任务: taskId={}, title={}", task.getId(), task.getTitle());
        UserContext userContext = getUserContext();
        if (userContext != null) {
            userContext.emit(UserContext.TaskStatusEvent.builder()
                    .type("task_start")
                    .taskId(task.getId())
                    .message("任务开始执行")
                    .build());
        }
        String input = getTaskInputString(state);
        AiService aiService = serviceNode.getAiService();
        if (aiService == null) {
            log.error("ServiceNode的AiService为空: taskId={}", task.getId());
            // 发送失败事件
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type("task_failed")
                        .taskId(task.getId())
                        .message("AiService未初始化")
                        .error("AiService为空")
                        .build());
            }
            throw new IllegalStateException("ServiceNode的AiService为空: taskId=" + task.getId());
        }

        try {
            TokenStream tokenStream = aiService.chat(input);
            StringBuilder resultBuilder = new StringBuilder();
            List<ChatMessage> messages = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(1);
            tokenStream.onPartialResponse(token -> {
                resultBuilder.append(token);
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type("streaming")
                            .taskId(task.getId())
                            .content(token)
                            .build());
                }
            });
            tokenStream.onCompleteResponse(e -> {
                AiMessage aiMessage = e.aiMessage();
                if (aiMessage != null) {
                    messages.add(aiMessage);

                }
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type("task_complete")
                            .taskId(task.getId())
                            .message("任务执行完成")
                            .build());
                }
                latch.countDown();
            });
            tokenStream.onError(error -> {
                log.error("流式输出执行器执行失败: taskId={}, error={}", task.getId(), error.getMessage());
                if (userContext != null) {
                    userContext.emit(UserContext.TaskStatusEvent.builder()
                            .type("task_failed")
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
                            .type("task_failed")
                            .taskId(task.getId())
                            .message("任务执行被中断")
                            .error("等待token流完成被中断")
                            .build());
                }
                throw new RuntimeException("等待token流完成被中断: taskId=" + task.getId(), e);
            }
            String result = resultBuilder.toString();
            Map<String, Object> stateUpdate = saveTaskResult(state, result);
            log.info("流式输出执行器完成: taskId={}, resultLength={}", task.getId(), result.length());
            return stateUpdate;
        } catch (Exception e) {
            log.error("流式输出执行器运行时异常: taskId={}, error={}", task.getId(), e.getMessage(), e);
            if (userContext != null) {
                userContext.emit(UserContext.TaskStatusEvent.builder()
                        .type("task_failed")
                        .taskId(task.getId())
                        .message("任务执行失败")
                        .error(e.getMessage())
                        .build());
            }
            throw e;
        }
    }
}

