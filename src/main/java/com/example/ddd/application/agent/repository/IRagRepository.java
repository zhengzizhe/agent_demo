package com.example.ddd.application.agent.repository;

import com.example.ddd.domain.agent.model.entity.RagEntity;

import java.util.List;

/**
 * Rag仓储接口
 */
public interface IRagRepository {

    /**
     * 插入Rag
     */
    RagEntity insert(RagEntity ragEntity);

    /**
     * 根据ID查询Rag
     */
    RagEntity queryById(String id);

    /**
     * 查询所有Rag
     */
    List<RagEntity> queryAll();

    /**
     * 根据Orchestrator ID查询关联的RAG列表
     */
    List<RagEntity> queryByOrchestratorId(String orchestratorId);

    /**
     * 根据Agent ID查询RAG列表
     */
    List<RagEntity> queryByAgentId(String agentId);

    /**
     * 更新Rag
     */
    int update(RagEntity ragEntity);

    /**
     * 根据ID删除Rag
     */
    int deleteById(String id);

}
