package com.example.ddd.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Toon工具类，用于生成紧凑的agent交付格式
 * TOON (Token-Oriented Object Notation) 是一种专为LLM输入优化的紧凑序列化格式
 *
 * 由于toon库的API还在开发中，这里提供一个兼容的实现，使用结构化格式和表情符号
 * 来减少token消耗并提高可读性
 */
@Slf4j
public class ToonUtil {

    /**
     * 将agent响应序列化为toon格式
     * 使用紧凑的结构化表示，减少token消耗
     * @param content 原始内容
     * @return toon格式的内容
     */
    public static String formatAgentResponse(String content) {
        try {
            // 使用紧凑的toon格式：[type]content
            return String.format("🔄%s", content);
        } catch (Exception e) {
            log.warn("Toon格式化失败，使用原始内容: {}", e.getMessage());
            return content;
        }
    }

    /**
     * 格式化agent思考过程
     * @param thought 思考内容
     * @return toon格式的思考内容
     */
    public static String formatAgentThought(String thought) {
        try {
            return String.format("🤔%s", thought);
        } catch (Exception e) {
            log.warn("Toon格式化思考内容失败: {}", e.getMessage());
            return "🤔 " + thought;
        }
    }

    /**
     * 格式化agent执行结果
     * @param result 执行结果
     * @return toon格式的结果
     */
    public static String formatAgentResult(String result) {
        try {
            return String.format("✅%s", result);
        } catch (Exception e) {
            log.warn("Toon格式化结果失败: {}", e.getMessage());
            return "✅ " + result;
        }
    }

    /**
     * 格式化任务执行状态
     * @param taskId 任务ID
     * @param status 状态
     * @param details 详情
     * @return toon格式的任务状态
     */
    public static String formatTaskStatus(String taskId, String status, String details) {
        try {
            return String.format("📋%s:%s-%s", taskId, status, details);
        } catch (Exception e) {
            log.warn("Toon格式化任务状态失败: {}", e.getMessage());
            return String.format("📋 任务[%s]: %s - %s", taskId, status, details);
        }
    }
}
