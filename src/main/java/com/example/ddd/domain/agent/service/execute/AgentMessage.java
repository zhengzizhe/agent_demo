package com.example.ddd.domain.agent.service.execute;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Serdeable
@Getter
@Setter
public class AgentMessage {

    /**
     * 动作类型: final | execute | summarize | clarify
     */
    private Action action;

    /**
     * 置信度: 0.0-1.0
     */
    private Double confidence;

    /**
     * 当 action=final 时给出的最终回答
     */
    private String answer;

    /**
     * 执行计划步骤列表
     */
    private List<String> plan;

    /**
     * 需要的资源或信息列表
     */
    private List<String> needs;

    /**
     * 简要原因
     */
    private String reason;

    /**
     * 任务的简要背景或输入摘要
     */
    private String context;

    /**
     * 状态: pending | running | completed | failed
     */
    private Status status;

    /**
     * 元数据
     */
    private Metadata metadata;

    /**
     * 动作类型枚举
     */
    @Serdeable
    public enum Action {
        final_("final"),
        execute("execute"),
        summarize("summarize"),
        clarify("clarify");

        private final String value;

        Action(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Action fromValue(String value) {
            for (Action action : Action.values()) {
                if (action.value.equals(value)) {
                    return action;
                }
            }
            throw new IllegalArgumentException("Unknown Action value: " + value);
        }
    }

    /**
     * 状态枚举
     */
    @Serdeable
    public enum Status {
        pending("pending"),
        running("running"),
        completed("completed"),
        failed("failed");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Status fromValue(String value) {
            for (Status status : Status.values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown Status value: " + value);
        }
    }

    /**
     * 优先级枚举
     */
    @Serdeable
    public enum Priority {
        P0("P0"),
        P1("P1"),
        P2("P2");

        private final String value;

        Priority(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }

        @JsonCreator
        public static Priority fromValue(String value) {
            for (Priority priority : Priority.values()) {
                if (priority.value.equals(value)) {
                    return priority;
                }
            }
            throw new IllegalArgumentException("Unknown Priority value: " + value);
        }
    }

    /**
     * 元数据类
     */
    @Serdeable
    @Getter
    @Setter
    public static class Metadata {
        /**
         * 任务类型
         */
        private String task_type;

        /**
         * 优先级: P0 | P1 | P2
         */
        private Priority priority;

        /**
         * 假设列表
         */
        private List<String> assumptions;

        /**
         * 后续跟进列表
         */
        private List<String> followups;
    }

    // --- 兼容性方法：为了向后兼容，保留一些旧的方法名 ---

    /**
     * 兼容旧代码：获取 reply，映射到 answer
     */
    public String getReply() {
        return answer;
    }

    /**
     * 兼容旧代码：设置 reply，映射到 answer
     */
    public void setReply(String reply) {
        this.answer = reply;
    }

    /**
     * 兼容旧代码：判断是否为最终消息
     */
    public boolean isIs_final() {
        return action == Action.final_ || status == Status.completed;
    }

    /**
     * 兼容旧代码：设置是否为最终消息
     */
    public void setIs_final(boolean is_final) {
        if (is_final) {
            this.action = Action.final_;
            this.status = Status.completed;
        }
    }

    /**
     * 兼容旧代码：获取 step（从 plan 的长度推断，或返回 1）
     */
    public int getStep() {
        return plan != null && !plan.isEmpty() ? plan.size() : 1;
    }

    /**
     * 兼容旧代码：设置 step（创建 plan 列表）
     */
    public void setStep(int step) {
        // 这个方法保留但不做实际操作，因为 step 现在从 plan 推断
        // 如果需要，可以初始化 plan
        if (plan == null) {
            plan = new java.util.ArrayList<>();
        }
    }
}
