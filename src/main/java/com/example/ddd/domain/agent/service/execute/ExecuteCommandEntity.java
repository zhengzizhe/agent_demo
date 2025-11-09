package com.example.ddd.domain.agent.service.execute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 执行命令实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCommandEntity {
    /**
     * Agent ID
     */
    private Long agentId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 会话ID（可选）
     */
    private String sessionId;
}




