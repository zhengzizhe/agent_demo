package com.example.ddd.domain.agent.service.execute.plan;

import com.example.ddd.domain.agent.service.execute.task.Task;
import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Supervisor返回的结构化计划
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Serdeable
@Setter
@Getter
public class SupervisorPlan {

    /**
     * 是否继续执行
     * false: 直接返回greeting，不执行任务
     * true: 执行任务列表
     */
    private Boolean shouldProceed;

    /**
     * 执行模式：PARALLEL（并行）、SERIAL（串行）、HYBRID（混合）
     */
    private ExecutionMode executionMode;

    /**
     * 用户开场白
     * 在开始执行前向用户展示的消息
     */
    private String greeting;

    /**
     * 消息处理策略
     * AUTO: 自动处理后续消息
     * IGNORE: 忽略后续消息
     */
    private MessageHandling messageHandling;

    /**
     * 任务列表
     */
    private List<Task> tasks = new ArrayList<>();
}





