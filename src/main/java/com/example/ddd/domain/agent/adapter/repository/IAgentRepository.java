package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.AgentEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
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
     * 根据Orchestrator ID查询Model映射（agentId -> List<ChatModelEntity>）
     */
    Map<Long, List<ChatModelEntity>> queryModelMapByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Orchestrator ID查询RAG映射（agentId -> List<RagEntity>）
     */
    Map<Long, List<RagEntity>> queryRagMapByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Agent ID查询Model列表
     */
    List<ChatModelEntity> queryModelsByAgentId(DSLContext dslContext, Long agentId);

    /**
     * 根据Agent ID查询RAG列表
     */
    List<RagEntity> queryRagsByAgentId(DSLContext dslContext, Long agentId);

    /**
     * 根据Agent ID查询MCP ID列表
     */
    List<Long> queryMcpIdsByAgentId(DSLContext dslContext, Long agentId);
}

