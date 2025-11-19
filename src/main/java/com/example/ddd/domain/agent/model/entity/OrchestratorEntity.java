package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Orchestrator实体（原Agent实体）
 */
@Getter
@Setter
@Serdeable
public class OrchestratorEntity {
    private Long id;
    private String name;
    private String description;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}


