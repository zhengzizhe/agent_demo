package com.example.ddd.domain.agent.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * MCP实体
 * 对应数据库表：public.mcp
 * 对应JSON格式：
 * {
 * "type": "sse",
 * "description": "...",
 * "isActive": true,
 * "name": "...",
 * "baseUrl": "...",
 * "headers": { "Authorization": "Bearer ..." }
 * }
 */
@Getter
@Setter
public class McpEntity {
    private String id;
    private String name;
    private String type;
    private String description;
    private String baseUrl;    // 对应数据库字段：base_url
    private Map<String, String> headers;  // 对应数据库字段：headers (JSONB)
    private String status;     // 对应数据库字段：status，ACTIVE或INACTIVE（从isActive推导）
    private Long createdAt;    // 对应数据库字段：created_at
    private Long updatedAt;    // 对应数据库字段：updated_at
}

