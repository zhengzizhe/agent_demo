package com.example.ddd.domain.agent.service.execute.blackBoard;

import java.util.Map;

public class InMemoryBlackboard implements Blackboard {
    private final java.util.concurrent.ConcurrentMap<String, String> map =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public void put(String k, String v) {
        map.put(k, v);
    }

    @Override
    public String get(String k) {
        return map.get(k);
    }

    @Override
    public java.util.Map<String, String> snapshot() {
        return java.util.Map.copyOf(map);
    }

    @Override
    public long appendMessage(long runId, Long taskId, String role, String kind, String content, Map<String, Object> meta) {
        return 0;
    }
}