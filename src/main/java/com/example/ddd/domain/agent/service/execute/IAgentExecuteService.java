package com.example.ddd.domain.agent.service.execute;

import io.micronaut.http.sse.Event;
import jakarta.inject.Singleton;
import reactor.core.publisher.FluxSink;

/**
 * Agent执行服务接口
 */
@Singleton
public interface IAgentExecuteService {
    /**
     * 执行Agent
     *
     * @param agentId Agent ID
     * @param message 用户消息
     * @return 执行结果
     */
    String execute(Long agentId, String message);

    /**
     * 流式执行Agent（支持SSE逐字符输出）
     *
     * @param agentId Agent ID
     * @param message 用户消息
     * @param emitter SSE事件发射器
     */
    void StreamExecute(Long agentId, String message, FluxSink<String> emitter);
}

