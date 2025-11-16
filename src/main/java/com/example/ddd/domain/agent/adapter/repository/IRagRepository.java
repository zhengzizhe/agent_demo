package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.RagEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Rag仓储接口
 */
public interface IRagRepository {

    /**
     * 插入Rag
     */
    RagEntity insert(DSLContext dslContext, RagEntity ragEntity);

    /**
     * 根据ID查询Rag
     */
    RagEntity queryById(DSLContext dslContext, Long id);

    /**
     * 查询所有Rag
     */
    List<RagEntity> queryAll(DSLContext dslContext);

    /**
     * 根据Orchestrator ID查询关联的RAG列表
     */
    List<RagEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Agent ID查询RAG列表
     */
    List<RagEntity> queryByAgentId(DSLContext dslContext, Long agentId);

    /**
     * 更新Rag
     */
    int update(DSLContext dslContext, RagEntity ragEntity);

    /**
     * 根据ID删除Rag
     */
    int deleteById(DSLContext dslContext, Long id);

}
