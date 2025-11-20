package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.infrastructure.dao.IDocKgEntityDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

/**
 * 文档-知识图谱实体关联仓储
 */
@Singleton
public class DocKgEntityRepository {
    
    @Inject
    private IDocKgEntityDao docKgEntityDao;

    /**
     * 批量插入文档-实体关联
     */
    public void batchInsert(DSLContext dsl, String docId, List<Long> entityIds) {
        docKgEntityDao.batchInsert(dsl, docId, entityIds);
    }

    /**
     * 根据文档ID查询关联的实体ID列表
     */
    public List<Long> queryEntityIdsByDocId(DSLContext dsl, String docId) {
        return docKgEntityDao.queryEntityIdsByDocId(dsl, docId);
    }

    /**
     * 根据实体ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByEntityId(DSLContext dsl, Long entityId) {
        return docKgEntityDao.queryDocIdsByEntityId(dsl, entityId);
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(DSLContext dsl, String docId) {
        return docKgEntityDao.deleteByDocId(dsl, docId);
    }

    /**
     * 删除实体的所有关联
     */
    public int deleteByEntityId(DSLContext dsl, Long entityId) {
        return docKgEntityDao.deleteByEntityId(dsl, entityId);
    }

}

