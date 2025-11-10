package com.example.ddd.domain.agent.service.execute;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 执行上下文
 */
@Getter
@Setter
public class ExecuteContext {
    /**
     * 当前处理的消息
     */
    private String message;

    /**
     * 当前处理的结果
     */
    private String result;

    /**
     * 存储额外的上下文数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 当前步骤
     */
    private int step = 1;

    /**
     * 当前客户端ID
     */
    private Long currentClientId;

    public <T> void put(String key, T value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
}




