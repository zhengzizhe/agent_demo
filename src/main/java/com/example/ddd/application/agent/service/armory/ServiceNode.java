package com.example.ddd.application.agent.service.armory;

import com.example.ddd.application.agent.service.execute.role.AgentRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service节点
 * 封装AiService和Client信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceNode {
    /**
     * AiService实例
     */
    private AiService aiService;

    /**
     * Client ID
     */
    private String clientId;

    /**
     * Client名称
     */
    private String clientName;

    /**
     * Client描述
     */
    private String clientDescription;

    /**
     * Client状态
     */
    private String clientStatus;

    /**
     * Client系统提示词
     */
    private String systemPrompt;

    /**
     * Client角色
     */
    private AgentRole role;

    /**
     * 获取Client ID（便捷方法）
     */
    public String getId() {
        return clientId;
    }

    /**
     * 获取Client名称（便捷方法）
     */
    public String getName() {
        return clientName;
    }
}
