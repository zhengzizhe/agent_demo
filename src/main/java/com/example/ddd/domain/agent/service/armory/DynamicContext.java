package com.example.ddd.domain.agent.service.armory;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Serdeable
public class DynamicContext {
    private Map<String, String> context = new HashMap<>();

    void put(String key, String value) {
        context.put(key, value);
    }
    String get(String key) {
        return context.get(key);
    }

}