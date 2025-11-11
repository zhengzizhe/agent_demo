package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * Client实体
 */
@Getter
@Setter
@Serdeable
public class ClientEntity {
    private Long id;
    private Long agentId;
    private String name;
    private String description;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private String systemPrompt;

}















