package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;

/**
 * Client仓储接口
 */
public interface IClientRepository {
    /**
     * 根据Agent ID查询关联的Client列表
     */
    List<ClientEntity> queryByAgentId(DSLContext dslContext, Long agentId);
    
    /**
     * 根据ID查询Client
     */
    ClientEntity queryById(DSLContext dslContext, Long id);
    
    /**
     * 根据Client ID查询关联的Model列表
     */
    List<ChatModelEntity> queryModelsByClientId(DSLContext dslContext, Long clientId);
    
    /**
     * 根据Client ID查询关联的RAG列表
     */
    List<RagEntity> queryRagsByClientId(DSLContext dslContext, Long clientId);
    
    /**
     * 根据Client ID查询关联的MCP ID列表
     */
    List<Long> queryMcpIdsByClientId(DSLContext dslContext, Long clientId);
    
    /**
     * 根据Client列表构建 modelId -> ragId 的映射关系
     * 假设在同一个Client中，Model和RAG按顺序一一对应
     */
    Map<Long, Long> buildModelToRagMap(DSLContext dslContext, List<ClientEntity> clients);
    
    /**
     * 根据Agent ID查询所有Client关联的Model Map
     * key: clientId, value: 该Client关联的Model列表
     */
    Map<Long, List<ChatModelEntity>> queryModelMapByAgentId(DSLContext dslContext, Long agentId);
    
    /**
     * 根据Agent ID查询所有Client关联的RAG Map
     * key: clientId, value: 该Client关联的RAG列表
     */
    Map<Long, List<RagEntity>> queryRagMapByAgentId(DSLContext dslContext, Long agentId);
}

