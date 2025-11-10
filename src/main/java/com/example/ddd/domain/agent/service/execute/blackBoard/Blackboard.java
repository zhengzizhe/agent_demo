package com.example.ddd.domain.agent.service.execute.blackBoard;

import java.util.Map;

public interface Blackboard {
    void put(String key, String value);

    String get(String key);

    Map<String, String> snapshot();

    long appendMessage(long runId, Long taskId, String role, String kind,
                       String content, Map<String, Object> meta); // 持久化日志
}
