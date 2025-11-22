package com.example.ddd.domain.agent.model.entity;

import com.example.ddd.application.agent.service.execute.role.AgentRole;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent实体（原Client实体）
 */
@Getter
@Setter
public class AgentEntity {
    private String id;
    private String orchestratorId;
    private String name;
    private String description;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private String systemPrompt;
    private AgentRole role;

}
