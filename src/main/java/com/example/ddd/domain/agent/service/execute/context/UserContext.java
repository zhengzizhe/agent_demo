package com.example.ddd.domain.agent.service.execute.context;

import io.micronaut.serde.annotation.Serdeable;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


@Slf4j
public class UserContext {
    private static final ThreadLocal<UserContext> CONTEXT_HOLDER = new ThreadLocal<>();
    private final BlockingQueue<TaskStatusEvent> eventQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread dispatcherThread;
    private FluxSink<TaskStatusEvent> emitter;

    public UserContext() {
        this.eventQueue = new LinkedBlockingQueue<>();
    }

    /**
     * 启动事件分发器（守护线程）
     * 从队列中取出事件并通过 FluxSink 发送
     *
     * @param emitter FluxSink 用于发送事件
     */
    public void startEventDispatcher(FluxSink<TaskStatusEvent> emitter) {
        if (this.emitter != null) {
            log.warn("事件分发器已经启动，忽略重复启动");
            return;
        }
        this.emitter = emitter;
        this.running.set(true);

        this.dispatcherThread = new Thread(() -> {
            log.debug("事件分发器守护线程启动");
            try {
                while (running.get() || !eventQueue.isEmpty()) {
                    try {
                        // 从队列中取出事件，最多等待1秒
                        TaskStatusEvent event = eventQueue.poll(1, TimeUnit.SECONDS);
                        if (event != null) {
                            if (emitter != null && !emitter.isCancelled()) {
                                emitter.next(event);
                                log.debug("事件分发器发送事件: type={}, taskId={}", event.getType(), event.getTaskId());
                            } else {
                                log.warn("Emitter已取消或为空，停止事件分发");
                                break;
                            }
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.debug("事件分发器被中断");
                        break;
                    } catch (Exception e) {
                        log.error("事件分发器发送事件失败: {}", e.getMessage(), e);
                    }
                }
                // 队列为空且不再运行时，发送完成事件
                if (emitter != null && !emitter.isCancelled()) {
                    emitter.complete();
                    log.debug("事件分发器完成");
                }
            } catch (Exception e) {
                log.error("事件分发器异常: {}", e.getMessage(), e);
                if (emitter != null && !emitter.isCancelled()) {
                    emitter.error(e);
                }
            }
        }, "UserContext-EventDispatcher");

        this.dispatcherThread.setDaemon(true);
        this.dispatcherThread.start();
        log.debug("事件分发器守护线程已启动");
    }

    /**
     * 停止事件分发器
     */
    public void stopEventDispatcher() {
        if (running.compareAndSet(true, false)) {
            log.debug("停止事件分发器");
            if (dispatcherThread != null) {
                dispatcherThread.interrupt();
            }
        }
    }

    /**
     * 设置当前线程的 UserContext
     */
    public static void set(UserContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前线程的 UserContext
     */
    public static UserContext get() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的 UserContext
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * 发送事件到队列（线程安全）
     *
     * @param event 事件数据
     */
    public void emit(TaskStatusEvent event) {
        try {
            if (event == null) {
                log.warn("尝试发送空事件，忽略");
                return;
            }
            boolean offered = eventQueue.offer(event);
            if (offered) {
                log.debug("事件已加入队列: type={}, taskId={}", event.getType(), event.getTaskId());
            } else {
                log.warn("事件队列已满，无法添加事件: type={}, taskId={}", event.getType(), event.getTaskId());
            }
        } catch (Exception e) {
            log.error("添加事件到队列失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 发送完成事件（标记停止，等待队列处理完）
     */
    public void complete() {
        log.info("标记事件分发器完成，等待队列处理完");
        stopEventDispatcher();
    }

    /**
     * 发送错误事件
     *
     * @param error 错误信息
     */
    public void error(String error) {
        TaskStatusEvent event = TaskStatusEvent.builder()
                .type("error")
                .error(error)
                .build();
        emit(event);
        log.error("用户上下文发送错误: {}", error);
    }

    /**
     * 任务状态事件
     */
    @Serdeable
    public static class TaskStatusEvent {
        private String type;
        private String taskId;
        private String message;
        private String content;
        private String error;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        /**
         * 创建事件构建器
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * 事件构建器
         */
        public static class Builder {
            private final TaskStatusEvent event = new TaskStatusEvent();

            public Builder type(String type) {
                event.setType(type);
                return this;
            }

            public Builder taskId(String taskId) {
                event.setTaskId(taskId);
                return this;
            }

            public Builder message(String message) {
                event.setMessage(message);
                return this;
            }

            public Builder content(String content) {
                event.setContent(content);
                return this;
            }

            public Builder error(String error) {
                event.setError(error);
                return this;
            }

            public TaskStatusEvent build() {
                return event;
            }
        }
    }
}

