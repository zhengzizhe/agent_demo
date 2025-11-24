package com.example.ddd.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 评分重试工具类
 * 根据评分结果决定是否重试
 */
@Slf4j
@Component
public class ScoredRetry {

    /**
     * 执行带评分的重试
     * 
     * @param task 要执行的任务，返回评分（0-100，分数越高越好）
     * @param minScore 最低可接受分数，低于此分数会重试
     * @param maxRetries 最大重试次数
     * @param delayMs 重试延迟（毫秒）
     * @return 最终评分
     */
    public int execute(Supplier<Integer> task, int minScore, int maxRetries, long delayMs) {
        int attempt = 0;
        int lastScore = 0;
        
        while (attempt <= maxRetries) {
            try {
                lastScore = task.get();
                log.debug("评分重试: attempt={}, score={}, minScore={}", attempt, lastScore, minScore);
                
                // 如果评分达到最低要求，直接返回
                if (lastScore >= minScore) {
                    log.info("评分重试成功: attempt={}, score={}", attempt, lastScore);
                    return lastScore;
                }
                
                // 评分不够，需要重试
                if (attempt < maxRetries) {
                    log.warn("评分不足，准备重试: attempt={}, score={}, minScore={}, delay={}ms", 
                        attempt, lastScore, minScore, delayMs);
                    Thread.sleep(delayMs);
                }
                
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("评分重试被中断: attempt={}", attempt);
                throw new RuntimeException("评分重试被中断", e);
            } catch (Exception e) {
                log.error("评分重试执行异常: attempt={}, error={}", attempt, e.getMessage(), e);
                // 发生异常时，如果还有重试机会，继续重试
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("评分重试被中断", ie);
                    }
                    attempt++;
                    continue;
                }
                throw new RuntimeException("评分重试失败", e);
            }
        }
        
        log.warn("评分重试达到最大次数: maxRetries={}, finalScore={}, minScore={}", 
            maxRetries, lastScore, minScore);
        return lastScore;
    }

    /**
     * 执行带评分的重试（默认配置）
     * 
     * @param task 要执行的任务
     * @param minScore 最低可接受分数
     * @return 最终评分
     */
    public int execute(Supplier<Integer> task, int minScore) {
        return execute(task, minScore, 3, 1000);
    }

    /**
     * 执行带评分的重试（带结果返回）
     * 
     * @param task 要执行的任务，返回评分结果对象
     * @param minScore 最低可接受分数
     * @param maxRetries 最大重试次数
     * @param delayMs 重试延迟（毫秒）
     * @param <T> 结果类型
     * @return 评分结果对象
     */
    public <T extends ScoredResult> T executeWithResult(
            Supplier<T> task, 
            int minScore, 
            int maxRetries, 
            long delayMs) {
        int attempt = 0;
        T lastResult = null;
        
        while (attempt <= maxRetries) {
            try {
                lastResult = task.get();
                int score = lastResult.getScore();
                log.debug("评分重试: attempt={}, score={}, minScore={}", attempt, score, minScore);
                
                // 如果评分达到最低要求，直接返回
                if (score >= minScore) {
                    log.info("评分重试成功: attempt={}, score={}", attempt, score);
                    return lastResult;
                }
                
                // 评分不够，需要重试
                if (attempt < maxRetries) {
                    log.warn("评分不足，准备重试: attempt={}, score={}, minScore={}, delay={}ms", 
                        attempt, score, minScore, delayMs);
                    Thread.sleep(delayMs);
                }
                
                attempt++;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("评分重试被中断: attempt={}", attempt);
                throw new RuntimeException("评分重试被中断", e);
            } catch (Exception e) {
                log.error("评分重试执行异常: attempt={}, error={}", attempt, e.getMessage(), e);
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("评分重试被中断", ie);
                    }
                    attempt++;
                    continue;
                }
                throw new RuntimeException("评分重试失败", e);
            }
        }
        
        log.warn("评分重试达到最大次数: maxRetries={}, finalScore={}, minScore={}", 
            maxRetries, lastResult != null ? lastResult.getScore() : 0, minScore);
        return lastResult;
    }

    /**
     * 评分结果接口
     */
    public interface ScoredResult {
        /**
         * 获取评分（0-100）
         */
        int getScore();
    }
}



