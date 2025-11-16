package com.example.ddd.domain.agent.model.entity;

import com.example.ddd.domain.agent.service.execute.role.AgentRole;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent实体（原Client实体）
 */
@Getter
@Setter
@Serdeable
public class AgentEntity {
    private Long id;
    private Long orchestratorId;
    private String name;
    private String description;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private String systemPrompt;
    private AgentRole role;

}
