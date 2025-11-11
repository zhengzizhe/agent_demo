package com.example.ddd.domain.agent.service.execute.plan;

import dev.langchain4j.service.TokenStream;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 计划解析器
 * 负责从TokenStream中提取完整响应并解析为SupervisorPlan对象
 */
@Slf4j
public class PlanParser {
    public String extractCompleteResponse(TokenStream tokenStream, String source) {
        CompletableFuture<String> future = new CompletableFuture<>();
        StringBuilder buffer = new StringBuilder();
        
        tokenStream.onPartialResponse(token -> {
            buffer.append(token);
        });
        
        tokenStream.onCompleteResponse(response -> {
            String completeText = response.aiMessage().text();
            // 记录token消耗
            if (response.tokenUsage() != null) {
                log.info("Token消耗 [{}] - inputTokens={}, outputTokens={}, totalTokens={}",
                        source,
                        response.tokenUsage().inputTokenCount(),
                        response.tokenUsage().outputTokenCount(),
                        response.tokenUsage().totalTokenCount());
            } else {
                log.warn("Token消耗信息不可用 [{}]", source);
            }
            future.complete(completeText);
        });
        
        tokenStream.onError(error -> {
            log.error("TokenStream处理出错 [{}]", source, error);
            future.completeExceptionally(error);
        });
        
        tokenStream.start();
        try {
            return future.get(30, TimeUnit.SECONDS);  // 设置30秒超时
        } catch (Exception e) {
            log.error("提取TokenStream响应失败，返回已收集的部分 [{}]", source, e);
            return buffer.toString();  // 返回已收集的部分
        }
    }


}

