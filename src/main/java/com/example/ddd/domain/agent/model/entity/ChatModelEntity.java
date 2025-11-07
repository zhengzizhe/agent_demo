package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * ChatModel实体
 */
@Getter
@Setter
@Serdeable
public class ChatModelEntity {
    private Long id;
    private Long clientId;
    private String name;
    private String provider;
    private String modelType;
    private String apiEndpoint;
    private String apiKey;
    private Integer maxTokens;
    private Double temperature;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}
