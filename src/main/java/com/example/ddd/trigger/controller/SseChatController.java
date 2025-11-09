package com.example.ddd.trigger.controller;

import com.example.ddd.domain.agent.service.execute.AgentExecuteService;
import com.example.ddd.trigger.request.AgentExecuteRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.sse.Event;
import jakarta.inject.Inject;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

/**
 * SSE流式对话接口（使用Reactor Flux）
 */
@Controller("/openai")
public class SseChatController {

    @Inject
    private AgentExecuteService agentExecuteService;


    @Post(uri = "/sse/chat", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<String> sseChatPost(@Body AgentExecuteRequest request) {
        return Flux.<String>create(emitter -> {
                    try {
                        agentExecuteService.StreamExecute(request.getAgentId(), request.getMessage(), emitter);
                    } catch (Exception e) {
                        if (!emitter.isCancelled()) {
                            emitter.error(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
