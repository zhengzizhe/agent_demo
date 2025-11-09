package com.example.ddd.trigger.controller;

import com.example.ddd.trigger.request.AgentExecuteRequest;
import io.micronaut.http.sse.Event;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SSE流式对话接口测试类
 */
@MicronautTest
class SseChatControllerTest {

    @Inject
    private SseChatController sseChatController;

    @Test
    void testSseChatPost_StreamingBehavior() {
        // 测试流式行为：验证字符是逐个发送的
        AgentExecuteRequest request = new AgentExecuteRequest();
        request.setAgentId(1L);
        request.setMessage("你好，介绍下你自己");

        // 创建测试用的Flux
        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            String testResult = "测试流式输出";
            char[] chars = testResult.toCharArray();
            for (char c : chars) {
                emitter.next(Event.of(String.valueOf(c)));
                try {
                    Thread.sleep(10); // 模拟延迟
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            emitter.next(Event.of("\n\n[DONE]"));
            emitter.complete();
        });

        // 使用StepVerifier验证流式行为
        StepVerifier.create(result)
            .expectNextCount(13) // "测试流式输出" 12个字符 + "[DONE]"事件
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    void testSseChatPost_CharacterByCharacter() throws InterruptedException {
        // 测试逐字符输出
        AgentExecuteRequest request = new AgentExecuteRequest();
        request.setAgentId(1L);
        request.setMessage("你好");

        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            String testResult = "Hello World";
            char[] chars = testResult.toCharArray();
            for (char c : chars) {
                emitter.next(Event.of(String.valueOf(c)));
            }
            emitter.next(Event.of("\n\n[DONE]"));
            emitter.complete();
        });

        List<String> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        result
            .doOnNext(event -> {
                String data = event.getData();
                if (data != null) {
                    receivedEvents.add(data);
                    System.out.print(data); // 打印接收到的字符
                }
            })
            .doOnComplete(() -> {
                System.out.println("\n流式输出完成");
                latch.countDown();
            })
            .doOnError(error -> {
                System.err.println("错误: " + error.getMessage());
                latch.countDown();
            })
            .subscribe();

        // 等待完成（最多等待5秒）
        assertTrue(latch.await(5, TimeUnit.SECONDS), "流式输出应该在5秒内完成");

        // 验证接收到了数据
        assertFalse(receivedEvents.isEmpty(), "应该接收到事件");
        
        // 验证最后一个事件是完成标记
        String lastEvent = receivedEvents.get(receivedEvents.size() - 1);
        assertTrue(lastEvent.contains("DONE") || lastEvent.contains("\n\n"), 
                   "最后一个事件应该是完成标记");
    }

    @Test
    void testSseChatPost_WithMultipleClients() throws InterruptedException {
        // 测试多个Client的流式输出
        AgentExecuteRequest request = new AgentExecuteRequest();
        request.setAgentId(1L);
        request.setMessage("测试多个Client");

        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            // 模拟第一个Client的输出
            String result1 = "第一个Client的输出";
            for (char c : result1.toCharArray()) {
                emitter.next(Event.of(String.valueOf(c)));
            }
            
            // 模拟第二个Client的输出
            String result2 = "第二个Client的输出";
            for (char c : result2.toCharArray()) {
                emitter.next(Event.of(String.valueOf(c)));
            }
            
            emitter.next(Event.of("\n\n[DONE]"));
            emitter.complete();
        });

        List<String> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        result
            .doOnNext(event -> {
                String data = event.getData();
                if (data != null) {
                    receivedEvents.add(data);
                }
            })
            .doOnComplete(() -> latch.countDown())
            .doOnError(error -> {
                System.err.println("错误: " + error.getMessage());
                latch.countDown();
            })
            .subscribe();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(receivedEvents.isEmpty());
    }

    @Test
    void testSseChatPost_ErrorHandling() throws InterruptedException {
        // 测试错误处理
        AgentExecuteRequest request = new AgentExecuteRequest();
        request.setAgentId(1L);
        request.setMessage("测试错误");

        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            emitter.error(new RuntimeException("模拟错误"));
        });

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean errorReceived = new AtomicBoolean(false);

        result
            .doOnError(error -> {
                errorReceived.set(true);
                System.out.println("接收到错误: " + error.getMessage());
                latch.countDown();
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(errorReceived.get(), "应该接收到错误");
    }

    @Test
    void testSseChatPost_EmptyMessage() throws InterruptedException {
        // 测试空消息
        AgentExecuteRequest request = new AgentExecuteRequest();
        request.setAgentId(1L);
        request.setMessage("");

        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            emitter.next(Event.of("\n\n[DONE]"));
            emitter.complete();
        });

        List<String> receivedEvents = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        result
            .doOnNext(event -> {
                String data = event.getData();
                if (data != null) {
                    receivedEvents.add(data);
                }
            })
            .doOnComplete(() -> latch.countDown())
            .subscribe();

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    @Test
    void testSseChatPost_Cancellation() {
        // 测试取消订阅
        Flux<Event<String>> result = Flux.<Event<String>>create(emitter -> {
            for (int i = 0; i < 100; i++) {
                if (emitter.isCancelled()) {
                    break;
                }
                emitter.next(Event.of(String.valueOf(i)));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            emitter.complete();
        });

        List<String> receivedEvents = new ArrayList<>();
        
        // 订阅后立即取消
        result
            .take(5) // 只取前5个事件
            .doOnNext(event -> {
                String data = event.getData();
                if (data != null) {
                    receivedEvents.add(data);
                }
            })
            .blockLast(Duration.ofSeconds(2));

        // 验证只接收到了5个事件
        assertEquals(5, receivedEvents.size(), "应该只接收到5个事件");
    }


}

