package com.example.ddd.trigger.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.sse.Event;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * SSE测试接口
 * 每1秒发送一次"你好"，用于测试SSE流式效果
 */
@Controller("/test")
public class TestSseController {

    @Get(uri = "/sse", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<String> testSse() {
        return Flux.<String>create(emitter -> {

            try {
                // 流式执行Agent，逐字符发送
                emitter.next("你好");
                Thread.sleep(1000);
                emitter.next("你好");
                Thread.sleep(1000);
                emitter.next("你好");
                Thread.sleep(1000);
                emitter.next("你好");
                Thread.sleep(1000);

            } catch (Exception e) {
                if (!emitter.isCancelled()) {
                    emitter.error(e);
                }
            }


        }).subscribeOn(Schedulers.boundedElastic());

    }
}



