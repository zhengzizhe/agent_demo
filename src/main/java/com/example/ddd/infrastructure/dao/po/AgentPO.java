package com.example.ddd.infrastructure.dao.po;

import com.example.ddd.application.agent.service.execute.role.AgentRole;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent持久化对象
 */
@Getter
@Setter
public class AgentPO {
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
