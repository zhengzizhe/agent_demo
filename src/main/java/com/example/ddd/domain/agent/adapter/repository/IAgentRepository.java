package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.AgentEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;

/**
 * Agent仓储接口（原Client仓储）
 */
public interface IAgentRepository {

    /**
     * 根据Orchestrator ID查询关联的Agent列表
     */
    List<AgentEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Orchestrator ID查询Model映射（agentId -> ChatModelEntity），每个agent只查询第一个model
     */
    Map<Long, ChatModelEntity> queryModelMapByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Orchestrator ID查询RAG映射（agentId -> List<RagEntity>）
     */
    Map<Long, List<RagEntity>> queryRagMapByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Orchestrator ID查询MCP映射（agentId -> List<McpEntity>）
     */
    Map<Long, List<McpEntity>> queryMcpMapByOrchestratorId(DSLContext dslContext, Long orchestratorId);

}


