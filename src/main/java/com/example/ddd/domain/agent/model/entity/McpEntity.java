package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * MCP实体
 */
@Getter
@Setter
@Serdeable
public class McpEntity {
    private Long id;
    private String name;
    private String type;
    private String endpoint;
    private Object config;
    private String status;
}

