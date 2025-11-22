package com.example.ddd.domain.agent.repository;

import com.example.ddd.infrastructure.dao.mapper.DocKgEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 文档-知识图谱实体关联仓储
 */
@Repository
public class DocKgEntityRepository {
    
    @Autowired
    private DocKgEntityMapper docKgEntityMapper;

    /**
     * 批量插入文档-实体关联
     */
    public void batchInsert(String docId, List<String> entityIds) {
        docKgEntityMapper.batchInsert(docId, entityIds);
    }

    /**
     * 根据文档ID查询关联的实体ID列表
     */
    public List<String> queryEntityIdsByDocId(String docId) {
        return docKgEntityMapper.queryEntityIdsByDocId(docId);
    }

    /**
     * 根据实体ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByEntityId(String entityId) {
        return docKgEntityMapper.queryDocIdsByEntityId(entityId);
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(String docId) {
        return docKgEntityMapper.deleteByDocId(docId);
    }

    /**
     * 删除实体的所有关联
     */
    public int deleteByEntityId(String entityId) {
        return docKgEntityMapper.deleteByEntityId(entityId);
    }
}
