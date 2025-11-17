package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.OrchestratorEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Orchestrator仓储接口（原Agent仓储）
 */
public interface IOrchestratorRepository {

    /**
     * 插入Orchestrator
     */
    OrchestratorEntity insert(DSLContext dslContext, OrchestratorEntity orchestratorEntity);

    /**
     * 根据ID查询Orchestrator
     */
    OrchestratorEntity queryById(DSLContext dslContext, Long id);

    /**
     * 查询所有Orchestrator
     */
    List<OrchestratorEntity> queryAll(DSLContext dslContext);

    /**
     * 更新Orchestrator
     */
    int update(DSLContext dslContext, OrchestratorEntity orchestratorEntity);

    /**
     * 根据ID删除Orchestrator
     */
    int deleteById(DSLContext dslContext, Long id);

}

