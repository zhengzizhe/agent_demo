package com.example.ddd.domain.agent.service.execute.task;

/**
 * 任务状态
 */
public enum TaskStatus {
    PENDING,    // 未开始
    RUNNING,    // 执行中
    DONE,       // 已完成
    FAILED,     // 失败
    SKIPPED     // 被跳过（可选）
}
