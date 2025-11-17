package com.example.ddd.domain.agent.service.execute.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务计划
 * 简化的任务编排结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskPlan {
    /**
     * 计划摘要
     */
    @JsonProperty("summary")
    private String summary;

    /**
     * 总任务数
     */
    @JsonProperty("totalTasks")
    private Integer totalTasks;

    /**
     * 任务列表
     */
    @JsonProperty("tasks")
    private List<Task> tasks;

}

