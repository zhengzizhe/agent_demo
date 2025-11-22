package com.example.ddd.application.agent.repository;

import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;

import java.util.List;

/**
 * 向量文档仓储接口
 */
public interface IVectorDocumentRepository {

    /**
     * 插入向量文档
     */
    VectorDocumentEntity insert(VectorDocumentEntity entity);

    /**
     * 批量插入向量文档
     */
    int batchInsert(List<VectorDocumentEntity> entities);

    /**
     * 根据ID查询向量文档
     */
    VectorDocumentEntity queryById(String id);

    /**
     * 根据RAG ID查询所有向量文档
     */
    List<VectorDocumentEntity> queryByRagId(String ragId);

    /**
     * 向量相似度搜索
     *
     * @param ragId               RAG ID
     * @param queryEmbedding      查询向量
     * @param limit               返回结果数量限制
     * @param similarityThreshold 相似度阈值（0-1之间）
     * @return 相似文档列表，按相似度降序排列
     */
    List<VectorDocumentEntity> similaritySearch(String ragId,
                                                float[] queryEmbedding, int limit,
                                                double similarityThreshold);

    /**
     * 根据RAG ID删除所有向量文档
     */
    int deleteByRagId(String ragId);

    /**
     * 根据ID删除向量文档
     */
    int deleteById(String embeddingId);
}






























