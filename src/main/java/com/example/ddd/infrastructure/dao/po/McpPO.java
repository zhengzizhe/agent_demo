package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * MCP持久化对象
 */
@Getter
@Setter
public class McpPO {
    private String id;
    private String name;
    private String type;
    private String description;
    private String baseUrl;
    private String headers; // JSON字符串
    private String status;
    private Long createdAt;
    private Long updatedAt;
}




































