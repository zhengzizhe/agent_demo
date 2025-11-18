package com.example.ddd.trigger.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import reactor.core.publisher.Flux;

@Controller
public class TestController {
    @Get(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM)
    public Flux<String> stream() {
        return Flux.create(sink -> {

                try {
                    for (int i = 1; i <= 5; i++) {
                        sink.next("推送消息：" + i);
                        Thread.sleep(1000);
                    }
                    sink.complete();
                } catch (Exception e) {
                    sink.error(e);
                }

        });
    }

}
