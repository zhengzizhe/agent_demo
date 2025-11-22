package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.AgentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Agent Mapper接口
 */
@Mapper
public interface AgentMapper {

    /**
     * 插入Agent
     */
    int insert(AgentPO agentPO);

    /**
     * 根据ID查询Agent
     */
    AgentPO queryById(@Param("id") String id);

    /**
     * 根据名称查询Agent
     */
    AgentPO queryByName(@Param("name") String name);

    /**
     * 查询所有Agent
     */
    List<AgentPO> queryAll();

    /**
     * 根据状态查询Agent列表
     */
    List<AgentPO> queryByStatus(@Param("status") String status);

    /**
     * 更新Agent
     */
    int update(AgentPO agentPO);

    /**
     * 根据ID删除Agent
     */
    int deleteById(@Param("id") String id);

    /**
     * 批量删除Agent
     */
    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 根据Orchestrator ID查询关联的Agent列表（包含role和system_prompt）
     */
    List<AgentPO> queryByOrchestratorId(@Param("orchestratorId") String orchestratorId);

    /**
     * 根据Orchestrator ID查询Agent ID列表
     */
    List<String> queryAgentIdsByOrchestratorId(@Param("orchestratorId") String orchestratorId);
}

