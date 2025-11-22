package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IAgentRepository;
import com.example.ddd.domain.agent.model.entity.AgentEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.infrastructure.dao.mapper.AgentMapper;
import com.example.ddd.infrastructure.dao.mapper.McpMapper;
import com.example.ddd.infrastructure.dao.mapper.ModelMapper;
import com.example.ddd.infrastructure.dao.mapper.RagMapper;
import com.example.ddd.infrastructure.dao.po.AgentPO;
import com.example.ddd.infrastructure.dao.po.McpPO;
import com.example.ddd.infrastructure.dao.po.ModelPO;
import com.example.ddd.infrastructure.dao.po.RagPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Agent仓储实现（原Client仓储）
 * Repository 层只能引用 DAO 层（Mapper）
 */
@Repository
public class AgentRepository implements IAgentRepository {

    @Autowired
    private AgentMapper agentMapper;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RagMapper ragMapper;
    @Autowired
    private McpMapper mcpMapper;

    @Override
    public List<AgentEntity> queryByOrchestratorId(String orchestratorId) {
        List<AgentPO> pos = agentMapper.queryByOrchestratorId(orchestratorId);
        return pos.stream()
                .map(po -> {
                    AgentEntity entity = convertToEntity(po);
                    entity.setOrchestratorId(orchestratorId);
                    return entity;
                })
                .toList();
    }

    @Override
    public Map<String, ChatModelEntity> queryModelMapByOrchestratorId(String orchestratorId) {
        List<String> agentIds = agentMapper.queryAgentIdsByOrchestratorId(orchestratorId);
        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, ChatModelEntity> modelMap = new HashMap<>();
        for (String agentId : agentIds) {
            List<ModelPO> modelPOs = modelMapper.queryByAgentId(agentId);
            if (!modelPOs.isEmpty()) {
                ModelPO firstModel = modelPOs.get(0);
                ChatModelEntity entity = convertToChatModelEntity(firstModel);
                entity.setClientId(agentId);
                modelMap.put(agentId, entity);
            }
        }
        return modelMap;
    }

    @Override
    public Map<String, List<RagEntity>> queryRagMapByOrchestratorId(String orchestratorId) {
        List<String> agentIds = agentMapper.queryAgentIdsByOrchestratorId(orchestratorId);
        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<RagEntity>> ragMap = new HashMap<>();
        for (String agentId : agentIds) {
            List<RagPO> ragPOs = ragMapper.queryByAgentId(agentId);
            if (!ragPOs.isEmpty()) {
                List<RagEntity> rags = ragPOs.stream()
                        .map(po -> {
                            RagEntity entity = convertToRagEntity(po);
                            entity.setClientId(agentId);
                            return entity;
                        })
                        .collect(Collectors.toList());
                ragMap.put(agentId, rags);
            }
        }
        return ragMap;
    }

    @Override
    public Map<String, List<McpEntity>> queryMcpMapByOrchestratorId(String orchestratorId) {
        List<String> agentIds = agentMapper.queryAgentIdsByOrchestratorId(orchestratorId);
        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, List<McpEntity>> mcpMap = new HashMap<>();
        for (String agentId : agentIds) {
            List<McpPO> mcpPOs = mcpMapper.queryByAgentId(agentId);
            if (!mcpPOs.isEmpty()) {
                List<McpEntity> mcps = mcpPOs.stream()
                        .map(this::convertToMcpEntity)
                        .collect(Collectors.toList());
                mcpMap.put(agentId, mcps);
            }
        }
        return mcpMap;
    }

    // 转换方法
    private AgentEntity convertToEntity(AgentPO po) {
        if (po == null) return null;
        AgentEntity entity = new AgentEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        entity.setSystemPrompt(po.getSystemPrompt());
        entity.setRole(po.getRole());
        return entity;
    }

    private ChatModelEntity convertToChatModelEntity(ModelPO po) {
        if (po == null) return null;
        ChatModelEntity entity = new ChatModelEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setProvider(po.getProvider());
        entity.setModelType(po.getModelType());
        entity.setApiEndpoint(po.getApiEndpoint());
        entity.setApiKey(po.getApiKey());
        entity.setMaxTokens(po.getMaxTokens());
        entity.setTemperature(po.getTemperature());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private RagEntity convertToRagEntity(RagPO po) {
        if (po == null) return null;
        RagEntity entity = new RagEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setVectorStoreType(po.getVectorStoreType());
        entity.setEmbeddingModel(po.getEmbeddingModel());
        entity.setChunkSize(po.getChunkSize());
        entity.setChunkOverlap(po.getChunkOverlap());
        entity.setDatabaseType(po.getDatabaseType());
        entity.setDatabaseHost(po.getDatabaseHost());
        entity.setDatabasePort(po.getDatabasePort());
        entity.setDatabaseName(po.getDatabaseName());
        entity.setDatabaseUser(po.getDatabaseUser());
        entity.setDatabasePassword(po.getDatabasePassword());
        entity.setTableName(po.getTableName());
        entity.setDimension(po.getDimension());
        entity.setUseIndex(po.getUseIndex());
        entity.setIndexListSize(po.getIndexListSize());
        entity.setConfig(po.getConfig());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }

    private McpEntity convertToMcpEntity(McpPO po) {
        if (po == null) return null;
        McpEntity entity = new McpEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setType(po.getType());
        entity.setDescription(po.getDescription());
        entity.setBaseUrl(po.getBaseUrl());
        if (po.getHeaders() != null) {
            try {
                Map<String, String> headers = com.example.ddd.common.utils.JSON.parseObject(po.getHeaders(),
                        new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {
                        });
                entity.setHeaders(headers);
            } catch (Exception e) {
                entity.setHeaders(null);
            }
        } else {
            entity.setHeaders(null);
        }

        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
