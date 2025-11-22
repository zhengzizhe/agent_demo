package com.example.ddd.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG-文档关联 Mapper接口
 */
@Mapper
public interface RagDocMapper {

    /**
     * 插入RAG-文档关联
     */
    void insert(@Param("ragId") String ragId, @Param("docId") String docId);

    /**
     * 根据RAG ID查询关联的文档ID列表
     */
    List<String> queryDocIdsByRagId(@Param("ragId") String ragId);

    /**
     * 根据文档ID查询关联的RAG ID列表
     */
    List<String> queryRagIdsByDocId(@Param("docId") String docId);

    /**
     * 删除RAG的所有关联
     */
    int deleteByRagId(@Param("ragId") String ragId);

    /**
     * 删除文档的所有关联
     */
    int deleteByDocId(@Param("docId") String docId);

    /**
     * 删除特定的RAG-文档关联
     */
    int delete(@Param("ragId") String ragId, @Param("docId") String docId);
}

