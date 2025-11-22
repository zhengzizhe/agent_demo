package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IRagRepository;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.infrastructure.dao.mapper.RagMapper;
import com.example.ddd.infrastructure.dao.po.RagPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Rag仓储实现
 */
@Repository
public class RagRepository implements IRagRepository {

    @Autowired
    private RagMapper ragMapper;

    @Override
    public RagEntity insert(RagEntity ragEntity) {
        RagPO ragPO = convertToPO(ragEntity);
        ragMapper.insert(ragPO);
        return convertToEntity(ragPO);
    }

    @Override
    public RagEntity queryById(String id) {
        RagPO po = ragMapper.queryById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<RagEntity> queryAll() {
        return ragMapper.queryAll().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据Agent ID查询RAG列表（兼容方法，实际调用queryByAgentId）
     */
    public List<RagEntity> queryByClientId(String clientId) {
        return queryByAgentId(clientId);
    }

    @Override
    public List<RagEntity> queryByAgentId(String agentId) {
        List<RagPO> pos = ragMapper.queryByAgentId(agentId);
        return pos.stream()
                .map(po -> {
                    RagEntity entity = convertToEntity(po);
                    if (entity != null) {
                        entity.setClientId(agentId);
                    }
                    return entity;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据Orchestrator ID查询关联的RAG列表
     * 先查询orchestrator关联的agent IDs，然后查询这些agents的RAG
     */
    @Override
    public List<RagEntity> queryByOrchestratorId(String orchestratorId) {
        // 通过 Mapper XML 中的 SQL 查询
        List<RagPO> pos = ragMapper.queryByOrchestratorId(orchestratorId);
        return pos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(RagEntity ragEntity) {
        RagPO ragPO = convertToPO(ragEntity);
        return ragMapper.update(ragPO);
    }

    @Override
    public int deleteById(String id) {
        return ragMapper.deleteById(id);
    }

    // 转换方法
    private RagPO convertToPO(RagEntity entity) {
        if (entity == null) return null;
        RagPO po = new RagPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setVectorStoreType(entity.getVectorStoreType());
        po.setEmbeddingModel(entity.getEmbeddingModel());
        po.setChunkSize(entity.getChunkSize());
        po.setChunkOverlap(entity.getChunkOverlap());
        po.setDatabaseType(entity.getDatabaseType());
        po.setDatabaseHost(entity.getDatabaseHost());
        po.setDatabasePort(entity.getDatabasePort());
        po.setDatabaseName(entity.getDatabaseName());
        po.setDatabaseUser(entity.getDatabaseUser());
        po.setDatabasePassword(entity.getDatabasePassword());
        po.setTableName(entity.getTableName());
        po.setDimension(entity.getDimension());
        po.setUseIndex(entity.getUseIndex());
        po.setIndexListSize(entity.getIndexListSize());
        po.setConfig(entity.getConfig() != null ? entity.getConfig().toString() : null);
        po.setStatus(entity.getStatus());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    private RagEntity convertToEntity(RagPO po) {
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
}
