package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IVectorDocumentRepository;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.infrastructure.dao.mapper.VectorDocumentMapper;
import com.example.ddd.infrastructure.dao.po.VectorDocumentPO;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 向量文档仓储实现
 */
@Repository
public class VectorDocumentRepository implements IVectorDocumentRepository {

    @Autowired
    private VectorDocumentMapper vectorDocumentMapper;

    @Override
    public VectorDocumentEntity insert(VectorDocumentEntity entity) {
        VectorDocumentPO po = convertToPO(entity);
        vectorDocumentMapper.insert(po);
        return convertToEntity(po);
    }

    @Override
    public int batchInsert(List<VectorDocumentEntity> entities) {
        List<VectorDocumentPO> pos = entities.stream()
                .map(this::convertToPO)
                .collect(Collectors.toList());
        return vectorDocumentMapper.batchInsert(pos);
    }

    @Override
    public VectorDocumentEntity queryById(String id) {
        VectorDocumentPO po = vectorDocumentMapper.queryById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<VectorDocumentEntity> queryByRagId(String ragId) {
        return vectorDocumentMapper.queryByRagId(ragId).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<VectorDocumentEntity> similaritySearch(String ragId, 
                                                       float[] queryEmbedding, int limit, 
                                                       double similarityThreshold) {
        return vectorDocumentMapper.similaritySearch(ragId, queryEmbedding, limit, similarityThreshold)
                .stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int deleteByRagId(String ragId) {
        return vectorDocumentMapper.deleteByRagId(ragId);
    }

    @Override
    public int deleteById(String embeddingId) {
        return vectorDocumentMapper.deleteById(embeddingId);
    }

    /**
     * 根据embeddingId查询向量文档
     */
    public VectorDocumentEntity queryByEmbeddingId(String embeddingId) {
        VectorDocumentPO po = vectorDocumentMapper.queryByEmbeddingId(embeddingId);
        return po != null ? convertToEntity(po) : null;
    }

    // 转换方法
    private VectorDocumentPO convertToPO(VectorDocumentEntity entity) {
        if (entity == null) return null;
        VectorDocumentPO po = new VectorDocumentPO();
        po.setEmbeddingId(entity.getEmbeddingId());
        po.setRagId(entity.getRagId());
        po.setContent(entity.getText());
        po.setEmbedding(entity.getEmbedding());
        
        // 转换metadata Map到JSON字符串
        if (entity.getMetadata() != null) {
            String jsonStr = JSON.toJSON(entity.getMetadata());
            po.setMetadata(jsonStr);
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
        
        // 转换JSON字符串到Map
        if (po.getMetadata() != null) {
            try {
                Map<String, Object> metadata = JSON.parseObject(
                    po.getMetadata(), 
                    new TypeReference<Map<String, Object>>() {}
                );
                entity.setMetadata(metadata);
            } catch (Exception e) {
                entity.setMetadata(new HashMap<>());
            }
        }
        
        entity.setChunkIndex(po.getChunkIndex());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
