package com.example.ddd.application.agent.repository;

import com.example.ddd.domain.agent.model.entity.OrchestratorEntity;

import java.util.List;

/**
 * Orchestrator仓储接口（原Agent仓储）
 */
public interface IOrchestratorRepository {

    /**
     * 插入Orchestrator
     */
    OrchestratorEntity insert(OrchestratorEntity orchestratorEntity);

    /**
     * 根据ID查询Orchestrator
     */
    OrchestratorEntity queryById(String id);

    /**
     * 查询所有Orchestrator
     */
    List<OrchestratorEntity> queryAll();

    /**
     * 更新Orchestrator
     */
    int update(OrchestratorEntity orchestratorEntity);

    /**
     * 根据ID删除Orchestrator
     */
    int deleteById(String id);

}

