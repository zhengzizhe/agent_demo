package com.example.ddd.application.agent.repository;

import com.example.ddd.domain.agent.model.entity.ChatModelEntity;

import java.util.List;

/**
 * ChatModel仓储接口
 */
public interface IChatModelRepository {

    /**
     * 插入ChatModel
     */
    ChatModelEntity insert(ChatModelEntity chatModelEntity);

    /**
     * 根据ID查询ChatModel
     */
    ChatModelEntity queryById(String id);

    /**
     * 查询所有ChatModel
     */
    List<ChatModelEntity> queryAll();

    /**
     * 更新ChatModel
     */
    int update(ChatModelEntity chatModelEntity);

    /**
     * 根据ID删除ChatModel
     */
    int deleteById(String id);

    /**
     * 根据Orchestrator ID查询关联的ChatModel列表
     */
    List<ChatModelEntity> queryByOrchestratorId(String orchestratorId);

    /**
     * 根据Agent ID查询Model列表
     */
    List<ChatModelEntity> queryByAgentId(String agentId);

}
