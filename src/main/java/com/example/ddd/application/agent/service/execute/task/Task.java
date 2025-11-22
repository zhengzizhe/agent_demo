package com.example.ddd.application.agent.service.execute.task;

import com.example.ddd.application.agent.service.execute.executor.ExecutionStrategy;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    /**
     * 任务ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 任务标题
     */
    @JsonProperty("title")
    private String title;

    /**
     * 任务描述
     */
    @JsonProperty("description")
    private String description;

    /**
     * 负责的Agent角色列表
     */
    @JsonProperty("agentId")
    private String agentId;

    /**
     * 任务状态
     */
    @JsonProperty("status")
    private TaskStatus status;

    /**
     * 执行顺序
     */
    @JsonProperty("order")
    private Integer order;

    /**
     * 输入配置
     */
    @JsonProperty("inputs")
    private TaskInputs inputs;

    /**
     * 输出字段列表
     */
    @JsonProperty("outputs")
    private List<String> outputs;

    /**
     * 执行策略
     * 定义任务的执行方式
     * SILENT: 静默执行，只保存到state，不输出给用户
     * STREAMING: 流式输出，实时向用户输出
     */
    @JsonProperty("executionStrategy")
    private ExecutionStrategy executionStrategy;

    /**
     * 任务输入配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskInputs {
        /**
         * 来自用户输入
         */
        @JsonProperty("fromUser")
        private Boolean fromUser;

        /**
         * 来自任务输出（任务ID）
         */
        @JsonProperty("fromTask")
        private List<String> fromTask;

        /**
         * 输入字段列表
         */
        @JsonProperty("fields")
        private List<String> fields;
    }
}
