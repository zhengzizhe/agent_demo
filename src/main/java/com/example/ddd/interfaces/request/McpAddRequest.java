package com.example.ddd.interfaces.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * MCP添加请求
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
public class McpAddRequest {
    /**
     * MCP名称
     */
    private String name;

    /**
     * MCP类型（如：sse, http等）
     */
    private String type;

    /**
     * MCP服务器描述
     */
    private String description;

    /**
     * MCP服务器baseUrl（如：https://dashscope.aliyuncs.com/api/v1/mcps/ChatPPT/sse）
     */
    private String baseUrl;

    /**
     * MCP请求头（如：{"Authorization": "Bearer ..."}）
     */
    private Map<String, String> headers;

    /**
     * 是否激活（对应数据库的status字段：true -> ACTIVE, false -> INACTIVE）
     */
    private Boolean isActive;

    /**
     * 状态：ACTIVE, INACTIVE（如果isActive存在，优先使用isActive）
     */
    private String status;
}

