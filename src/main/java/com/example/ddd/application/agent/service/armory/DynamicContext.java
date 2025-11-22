package com.example.ddd.application.agent.service.armory;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DynamicContext {
    private Map<String, String> context = new HashMap<>();

    void put(String key, String value) {
        context.put(key, value);
    }

    String get(String key) {
        return context.get(key);
    }

}