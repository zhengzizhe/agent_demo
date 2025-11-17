package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.McpEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Mcp仓储接口
 */
public interface IMcpRepository {

    /**
     * 插入Mcp
     */
    McpEntity insert(DSLContext dslContext, McpEntity mcpEntity);

    /**
     * 根据ID查询Mcp
     */
    McpEntity queryById(DSLContext dslContext, Long id);

    /**
     * 查询所有Mcp
     */
    List<McpEntity> queryAll(DSLContext dslContext);

    /**
     * 根据Agent ID查询MCP列表
     */
    List<McpEntity> queryByAgentId(DSLContext dslContext, Long agentId);

    /**
     * 更新Mcp
     */
    int update(DSLContext dslContext, McpEntity mcpEntity);

    /**
     * 根据ID删除Mcp
     */
    int deleteById(DSLContext dslContext, Long id);

}
