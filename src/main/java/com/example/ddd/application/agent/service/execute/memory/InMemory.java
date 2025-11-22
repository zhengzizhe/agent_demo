package com.example.ddd.application.agent.service.execute.memory;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存中的对话历史管理
 * 使用 Map 存储，key 为 userId + sessionId
 */
@Slf4j
public class InMemory {

    private static final InMemory instance = new InMemory();

    /**
     * 对话历史存储
     * Key: userId + "_" + sessionId
     * Value: 对话消息列表
     */
    private final Map<String, List<ConversationMessage>> memoryMap = new ConcurrentHashMap<>();

    private InMemory() {
    }

    public static InMemory getInstance() {
        return instance;
    }

    /**
     * 获取对话历史
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @return 对话历史列表
     */
    public List<ConversationMessage> getHistory(String userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        List<ConversationMessage> history = memoryMap.get(key);
        if (history == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }

    /**
     * 获取最近的对话历史（限制数量）
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param limit     限制数量
     * @return 对话历史列表
     */
    public List<ConversationMessage> getRecentHistory(String userId, String sessionId, int limit) {
        List<ConversationMessage> history = getHistory(userId, sessionId);
        if (history.size() <= limit) {
            return history;
        }
        // 返回最近的 N 条
        return new ArrayList<>(history.subList(Math.max(0, history.size() - limit), history.size()));
    }

    /**
     * 添加对话消息
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param role      角色（USER 或 ASSISTANT）
     * @param content   内容
     */
    public void addMessage(String userId, String sessionId, String role, String content) {
        if (userId == null || sessionId == null) {
            log.warn("userId 或 sessionId 为空，跳过记忆存储: userId={}, sessionId={}", userId, sessionId);
            return;
        }
        if (content == null || content.trim().isEmpty()) {
            log.warn("内容为空，跳过记忆存储");
            return;
        }

        String key = buildKey(userId, sessionId);
        memoryMap.computeIfAbsent(key, k -> new ArrayList<>()).add(
                new ConversationMessage(role, content, System.currentTimeMillis())
        );

        log.debug("添加对话记忆: key={}, role={}, contentLength={}", key, role, content.length());
    }

    /**
     * 格式化对话历史为字符串（用于注入到 prompt）
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @param limit     限制数量
     * @return 格式化的对话历史字符串
     */
    public String formatHistoryForPrompt(String userId, String sessionId, int limit) {
        List<ConversationMessage> history = getRecentHistory(userId, sessionId, limit);
        if (history.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("以下是历史对话：\n\n");
        for (ConversationMessage msg : history) {
            if ("USER".equalsIgnoreCase(msg.getRole())) {
                sb.append("用户: ").append(msg.getContent()).append("\n");
            } else if ("ASSISTANT".equalsIgnoreCase(msg.getRole())) {
                sb.append("助手: ").append(msg.getContent()).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * 构建 Map 的 key
     */
    private String buildKey(String userId, String sessionId) {
        return (userId != null ? userId : "unknown") + "_" + (sessionId != null ? sessionId : "default");
    }

    /**
     * 清除指定会话的记忆
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     */
    public void clearHistory(String userId, String sessionId) {
        String key = buildKey(userId, sessionId);
        memoryMap.remove(key);
        log.debug("清除对话记忆: key={}", key);
    }

    /**
     * 对话消息
     */
    @Data
    public static class ConversationMessage {
        private String role;      // USER 或 ASSISTANT
        private String content;   // 消息内容
        private long timestamp;   // 时间戳

        public ConversationMessage(String role, String content, long timestamp) {
            this.role = role;
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}

