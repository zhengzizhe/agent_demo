package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.McpPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MCP Mapper接口
 */
@Mapper
public interface McpMapper {

    /**
     * 插入MCP
     */
    int insert(McpPO mcpPO);

    /**
     * 根据ID查询MCP
     */
    McpPO queryById(@Param("id") String id);

    /**
     * 根据名称查询MCP
     */
    McpPO queryByName(@Param("name") String name);

    /**
     * 根据类型查询MCP列表
     */
    List<McpPO> queryByType(@Param("type") String type);

    /**
     * 查询所有MCP
     */
    List<McpPO> queryAll();

    /**
     * 根据状态查询MCP列表
     */
    List<McpPO> queryByStatus(@Param("status") String status);

    /**
     * 更新MCP
     */
    int update(McpPO mcpPO);

    /**
     * 根据ID删除MCP
     */
    int deleteById(@Param("id") String id);

    /**
     * 批量删除MCP
     */
    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 根据Agent ID查询关联的MCP列表
     */
    List<McpPO> queryByAgentId(@Param("agentId") String agentId);
}

