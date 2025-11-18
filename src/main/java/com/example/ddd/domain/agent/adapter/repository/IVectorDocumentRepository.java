package com.example.ddd.domain.agent.adapter.repository;

import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import org.jooq.DSLContext;

import java.util.List;

/**
 * 向量文档仓储接口
 */
public interface IVectorDocumentRepository {

    /**
     * 插入向量文档
     */
    VectorDocumentEntity insert(DSLContext dslContext, VectorDocumentEntity entity);

    /**
     * 批量插入向量文档
     */
    int batchInsert(DSLContext dslContext, List<VectorDocumentEntity> entities);

    /**
     * 根据ID查询向量文档
     */
    VectorDocumentEntity queryById(DSLContext dslContext, Long id);

    /**
     * 根据RAG ID查询所有向量文档
     */
    List<VectorDocumentEntity> queryByRagId(DSLContext dslContext, Long ragId);

    /**
     * 向量相似度搜索
     * @param ragId RAG ID
     * @param queryEmbedding 查询向量
     * @param limit 返回结果数量限制
     * @param similarityThreshold 相似度阈值（0-1之间）
     * @return 相似文档列表，按相似度降序排列
     */
    List<VectorDocumentEntity> similaritySearch(DSLContext dslContext, Long ragId, 
                                                 float[] queryEmbedding, int limit, 
                                                 double similarityThreshold);

    /**
     * 根据RAG ID删除所有向量文档
     */
    int deleteByRagId(DSLContext dslContext, Long ragId);

    /**
     * 根据ID删除向量文档
     */
    int deleteById(DSLContext dslContext, Long id);
}





























