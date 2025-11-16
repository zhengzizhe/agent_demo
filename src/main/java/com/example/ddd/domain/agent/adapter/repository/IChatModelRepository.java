package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * ChatModel仓储接口
 */
public interface IChatModelRepository {

    /**
     * 插入ChatModel
     */
    ChatModelEntity insert(DSLContext dslContext, ChatModelEntity chatModelEntity);

    /**
     * 根据ID查询ChatModel
     */
    ChatModelEntity queryById(DSLContext dslContext, Long id);

    /**
     * 查询所有ChatModel
     */
    List<ChatModelEntity> queryAll(DSLContext dslContext);

    /**
     * 更新ChatModel
     */
    int update(DSLContext dslContext, ChatModelEntity chatModelEntity);

    /**
     * 根据ID删除ChatModel
     */
    int deleteById(DSLContext dslContext, Long id);

    /**
     * 根据Orchestrator ID查询关联的ChatModel列表
     */
    List<ChatModelEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId);

    /**
     * 根据Agent ID查询Model列表
     */
    List<ChatModelEntity> queryByAgentId(DSLContext dslContext, Long agentId);

}
