package com.example.ddd.domain.agent.model.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * Orchestrator实体（原Agent实体）
 */
@Getter
@Setter
public class OrchestratorEntity {
    private String id;
    private String name;
    private String description;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}


