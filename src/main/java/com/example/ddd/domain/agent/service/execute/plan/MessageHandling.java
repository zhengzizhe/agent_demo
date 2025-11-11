package com.example.ddd.domain.agent.service.execute.plan;

import io.micronaut.serde.annotation.Serdeable;

/**
 * 消息处理策略
 */
@Serdeable
public enum MessageHandling {
    /**
     * 自动处理：系统自动处理后续消息，无需用户确认
     */
    AUTO,
    /**
     * 忽略：忽略后续消息，直接完成
     */
    IGNORE
}











