package com.example.ddd.application.agent.repository;

import com.example.ddd.domain.agent.model.entity.McpEntity;

import java.util.List;

/**
 * Mcp仓储接口
 */
public interface IMcpRepository {

    /**
     * 插入Mcp
     */
    McpEntity insert(McpEntity mcpEntity);

    /**
     * 根据ID查询Mcp
     */
    McpEntity queryById(String id);

    /**
     * 查询所有Mcp
     */
    List<McpEntity> queryAll();

    /**
     * 根据Agent ID查询MCP列表
     */
    List<McpEntity> queryByAgentId(String agentId);

    /**
     * 更新Mcp
     */
    int update(McpEntity mcpEntity);

    /**
     * 根据ID删除Mcp
     */
    int deleteById(String id);

}
