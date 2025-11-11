package com.example.ddd.domain.agent.service.execute.plan;

import io.micronaut.serde.annotation.Serdeable;

/**
 * 任务执行模式
 */
@Serdeable
public enum ExecutionMode {
    /**
     * 并行执行：所有任务同时执行
     */
    PARALLEL,
    
    /**
     * 串行执行：按顺序依次执行
     */
    SERIAL,
    
    /**
     * 混合模式：按任务组并行，组内串行
     */
    HYBRID
}











