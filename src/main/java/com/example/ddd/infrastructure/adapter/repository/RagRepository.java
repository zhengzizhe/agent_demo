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
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;

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
     * 根据Client ID查询RAG列表
     */
    public List<RagEntity> queryByClientId(DSLContext dslContext, Long clientId) {
        return ragDao.queryByClientId(dslContext, clientId).stream()
                .map(po -> {
                    RagEntity entity = convertToEntity(po);
                    // 设置 clientId
                    if (entity != null) {
                        entity.setClientId(clientId);
                    }
                    return entity;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RagEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 先查询agent关联的client IDs
        List<Long> clientIds = dslContext.select(field("client_id"))
                .from(table("agent_client"))
                .where(field("agent_id").eq(agentId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("client_id"))
                .collect(Collectors.toList());
        
        if (clientIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 通过clientId查询RAG，并建立 ragId -> clientId 的映射
        Map<Long, Long> ragToClientMap = new java.util.HashMap<>();
        for (Long clientId : clientIds) {
            List<Long> ragIds = dslContext.select(field("rag_id"))
                    .from(table("client_rag"))
                    .where(field("client_id").eq(clientId))
                    .fetch()
                    .stream()
                    .map(record -> (Long) record.get("rag_id"))
                    .collect(Collectors.toList());
            for (Long ragId : ragIds) {
                ragToClientMap.putIfAbsent(ragId, clientId);
            }
        }
        
        // 查询这些 rag IDs 对应的 RAG
        List<Long> allRagIds = new ArrayList<>(ragToClientMap.keySet());
        if (allRagIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return ragDao.queryByClientIds(dslContext, clientIds).stream()
                .map(po -> {
                    RagEntity entity = convertToEntity(po);
                    // 设置 clientId
                    if (entity != null && ragToClientMap.containsKey(entity.getId())) {
                        entity.setClientId(ragToClientMap.get(entity.getId()));
                    }
                    return entity;
                })
                .collect(Collectors.toList());
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
