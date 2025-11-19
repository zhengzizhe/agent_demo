package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.adapter.repository.IVectorDocumentRepository;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.infrastructure.dao.IVectorDocumentDao;
import com.example.ddd.infrastructure.dao.po.VectorDocumentPO;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量文档仓储实现
 */
@Singleton
public class VectorDocumentRepository implements IVectorDocumentRepository {

    @Inject
    private IVectorDocumentDao vectorDocumentDao;

    @Override
    public VectorDocumentEntity insert(DSLContext dslContext, VectorDocumentEntity entity) {
        VectorDocumentPO po = convertToPO(entity);
        VectorDocumentPO result = vectorDocumentDao.insert(dslContext, po);
        return convertToEntity(result);
    }

    @Override
    public int batchInsert(DSLContext dslContext, List<VectorDocumentEntity> entities) {
        List<VectorDocumentPO> pos = entities.stream()
                .map(this::convertToPO)
                .collect(Collectors.toList());
        return vectorDocumentDao.batchInsert(dslContext, pos);
    }

    @Override
    public VectorDocumentEntity queryById(DSLContext dslContext, Long id) {
        VectorDocumentPO po = vectorDocumentDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<VectorDocumentEntity> queryByRagId(DSLContext dslContext, Long ragId) {
        return vectorDocumentDao.queryByRagId(dslContext, ragId).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VectorDocumentEntity> similaritySearch(DSLContext dslContext, Long ragId, 
                                                       float[] queryEmbedding, int limit, 
                                                       double similarityThreshold) {
        return vectorDocumentDao.similaritySearch(dslContext, ragId, queryEmbedding, limit, similarityThreshold)
                .stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByRagId(DSLContext dslContext, Long ragId) {
        return vectorDocumentDao.deleteByRagId(dslContext, ragId);
    }

    @Override
    public int deleteById(DSLContext dslContext, String embeddingId) {
        return vectorDocumentDao.deleteById(dslContext, embeddingId);
    }

    // 转换方法
    private VectorDocumentPO convertToPO(VectorDocumentEntity entity) {
        if (entity == null) return null;
        VectorDocumentPO po = new VectorDocumentPO();
        po.setEmbeddingId(entity.getEmbeddingId());
        po.setRagId(entity.getRagId());
        po.setContent(entity.getText());
        po.setEmbedding(entity.getEmbedding());
        
        // 转换metadata Map到JSONB
        if (entity.getMetadata() != null) {
            String jsonStr = JSON.toJSON(entity.getMetadata());
            if (jsonStr != null) {
                po.setMetadata(JSONB.valueOf(jsonStr));
            }
        }
        
        po.setChunkIndex(entity.getChunkIndex());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    private VectorDocumentEntity convertToEntity(VectorDocumentPO po) {
        if (po == null) return null;
        VectorDocumentEntity entity = new VectorDocumentEntity();
        entity.setEmbeddingId(po.getEmbeddingId());
        entity.setRagId(po.getRagId());
        entity.setText(po.getContent());
        entity.setEmbedding(po.getEmbedding());
        
        // 转换JSONB到Map
        if (po.getMetadata() != null) {
            try {
                String jsonStr = po.getMetadata().data();
                Map<String, Object> metadata = JSON.objectMapper.readValue(
                    jsonStr, 
                    new TypeReference<Map<String, Object>>() {}
                );
                entity.setMetadata(metadata);
            } catch (Exception e) {
                // 如果解析失败，设置为空Map
                entity.setMetadata(new HashMap<>());
            }
        }
        
        entity.setChunkIndex(po.getChunkIndex());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}


