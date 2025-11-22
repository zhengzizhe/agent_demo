package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.VectorDocumentPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 向量文档 Mapper接口
 */
@Mapper
public interface VectorDocumentMapper {

    /**
     * 插入向量文档
     */
    int insert(VectorDocumentPO vectorDocumentPO);

    /**
     * 批量插入向量文档
     */
    int batchInsert(@Param("documents") List<VectorDocumentPO> documents);

    /**
     * 根据ID查询向量文档
     */
    VectorDocumentPO queryById(@Param("id") String embeddingId);

    /**
     * 根据RAG ID查询所有向量文档
     */
    List<VectorDocumentPO> queryByRagId(@Param("ragId") String ragId);

    /**
     * 向量相似度搜索
     */
    List<VectorDocumentPO> similaritySearch(@Param("ragId") String ragId,
                                           @Param("queryEmbedding") float[] queryEmbedding,
                                           @Param("limit") int limit,
                                           @Param("similarityThreshold") double similarityThreshold);

    /**
     * 根据RAG ID删除所有向量文档
     */
    int deleteByRagId(@Param("ragId") String ragId);

    /**
     * 根据ID删除向量文档
     */
    int deleteById(@Param("embeddingId") String embeddingId);

    /**
     * 根据embeddingId查询向量文档
     */
    VectorDocumentPO queryByEmbeddingId(@Param("embeddingId") String embeddingId);
}

