package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.AgentEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * Agent仓储接口
 */
public interface IAgentRepository {

    /**
     * 插入Agent
     */
    AgentEntity insert(DSLContext dslContext, AgentEntity agentEntity);

    /**
     * 根据ID查询Agent
     */
    AgentEntity queryById(DSLContext dslContext, Long id);

    /**
     * 查询所有Agent
     */
    List<AgentEntity> queryAll(DSLContext dslContext);

    /**
     * 更新Agent
     */
    int update(DSLContext dslContext, AgentEntity agentEntity);

    /**
     * 根据ID删除Agent
     */
    int deleteById(DSLContext dslContext, Long id);

}
