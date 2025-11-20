package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.infrastructure.dao.IRagDao;
import com.example.ddd.infrastructure.dao.po.RagPO;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.jooq.tables.AgentRag.AGENT_RAG;
import static com.example.jooq.tables.OrchestratorAgent.ORCHESTRATOR_AGENT;

/**
 * Rag仓储实现
 */
@Singleton
public class RagRepository implements IRagRepository {

    @Inject
    private IRagDao ragDao;

    @Override
    public RagEntity insert(DSLContext dslContext, RagEntity ragEntity) {
        RagPO ragPO = convertToPO(ragEntity);
        RagPO result = ragDao.insert(dslContext, ragPO);
        return convertToEntity(result);
    }

    @Override
    public RagEntity queryById(DSLContext dslContext, Long id) {
        RagPO po = ragDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<RagEntity> queryAll(DSLContext dslContext) {
        return ragDao.queryAll(dslContext).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    /**
     * 根据Agent ID查询RAG列表（兼容方法，实际调用queryByAgentId）
     */
    public List<RagEntity> queryByClientId(DSLContext dslContext, Long clientId) {
        return queryByAgentId(dslContext, clientId);
    }

    @Override
    public List<RagEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 直接通过agentId查询agent_rag关联表
        List<Long> ragIds = dslContext.select(AGENT_RAG.RAG_ID)
                .from(AGENT_RAG)
                .where(AGENT_RAG.AGENT_ID.eq(agentId))
                .fetchInto(Long.class);
        
        if (ragIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 rag IDs 对应的 RAG
        return ragDao.queryByClientId(dslContext, agentId).stream()
                .map(po -> {
                    RagEntity entity = convertToEntity(po);
                    // 设置 agentId
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
    public List<RagEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
        // 先查询orchestrator关联的agent IDs
        List<Long> agentIds = dslContext.select(ORCHESTRATOR_AGENT.AGENT_ID)
                .from(ORCHESTRATOR_AGENT)
                .where(ORCHESTRATOR_AGENT.ORCHESTRATOR_ID.eq(orchestratorId))
                .fetchInto(Long.class);
        
        if (agentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些agents的RAG
        List<RagEntity> allRags = new ArrayList<>();
        for (Long agentId : agentIds) {
            allRags.addAll(queryByAgentId(dslContext, agentId));
        }
        
        return allRags;
    }

    @Override
    public int update(DSLContext dslContext, RagEntity ragEntity) {
        RagPO ragPO = convertToPO(ragEntity);
        return ragDao.update(dslContext, ragPO);
    }

    @Override
    public int deleteById(DSLContext dslContext, Long id) {
        return ragDao.deleteById(dslContext, id);
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

        if (entity.getDatabaseType() != null) {
            po.setDatabaseType(entity.getDatabaseType());
        }
        if (entity.getDatabaseHost() != null) {
            po.setDatabaseHost(entity.getDatabaseHost());
        }
        if (entity.getDatabasePort() != null) {
            po.setDatabasePort(entity.getDatabasePort());
        }
        if (entity.getDatabaseName() != null) {
            po.setDatabaseName(entity.getDatabaseName());
        }
        if (entity.getDatabaseUser() != null) {
            po.setDatabaseUser(entity.getDatabaseUser());
        }
        if (entity.getDatabasePassword() != null) {
            po.setDatabasePassword(entity.getDatabasePassword());
        }
        if (entity.getTableName() != null) {
            po.setTableName(entity.getTableName());
        }
        if (entity.getDimension() != null) {
            po.setDimension(entity.getDimension());
        }
        if (entity.getUseIndex() != null) {
            po.setUseIndex(entity.getUseIndex());
        }
        if (entity.getIndexListSize() != null) {
            po.setIndexListSize(entity.getIndexListSize());
        }
        po.setConfig(entity.getConfig() != null ? org.jooq.JSONB.valueOf(entity.getConfig().toString()) : null);
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
