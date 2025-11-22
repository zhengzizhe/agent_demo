package com.example.ddd.infrastructure.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档-知识图谱实体关联 Mapper接口
 */
@Mapper
public interface DocKgEntityMapper {

    /**
     * 批量插入文档-实体关联
     */
    void batchInsert(@Param("docId") String docId, @Param("entityIds") List<String> entityIds);

    /**
     * 根据文档ID查询关联的实体ID列表
     */
    List<String> queryEntityIdsByDocId(@Param("docId") String docId);

    /**
     * 根据实体ID查询关联的文档ID列表
     */
    List<String> queryDocIdsByEntityId(@Param("entityId") String entityId);

    /**
     * 删除文档的所有关联
     */
    int deleteByDocId(@Param("docId") String docId);

    /**
     * 删除实体的所有关联
     */
    int deleteByEntityId(@Param("entityId") String entityId);
}

