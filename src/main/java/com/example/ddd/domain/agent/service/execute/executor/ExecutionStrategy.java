package com.example.ddd.domain.agent.service.execute.executor;

/**
 * 任务执行策略
 * 定义任务的执行方式和输出行为
 */
public enum ExecutionStrategy {
    /**
     * 静默执行
     * - 只将结果保存到state中
     * - 不向用户输出任何内容
     * - 适用于：数据预处理、中间计算、后台任务
     */
    SILENT,

    /**
     * 流式输出
     * - 将结果保存到state
     * - 同时通过TokenStream向用户实时输出
     * - 适用于：生成报告、总结、说明等需要用户看到的内容
     */
    STREAMING,


}

