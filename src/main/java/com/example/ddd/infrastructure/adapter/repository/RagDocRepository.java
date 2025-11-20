package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.infrastructure.dao.IRagDocDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

/**
 * RAG-文档关联仓储
 */
@Singleton
public class RagDocRepository {
    
    @Inject
    private IRagDocDao ragDocDao;

    /**
     * 插入RAG-文档关联
     */
    public void insert(DSLContext dsl, Long ragId, String docId) {
        ragDocDao.insert(dsl, ragId, docId);
    }

    /**
     * 根据RAG ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByRagId(DSLContext dsl, Long ragId) {
        return ragDocDao.queryDocIdsByRagId(dsl, ragId);
    }

    /**
     * 根据文档ID查询关联的RAG ID列表
     */
    public List<Long> queryRagIdsByDocId(DSLContext dsl, String docId) {
        return ragDocDao.queryRagIdsByDocId(dsl, docId);
    }

    /**
     * 删除RAG的所有关联
     */
    public int deleteByRagId(DSLContext dsl, Long ragId) {
        return ragDocDao.deleteByRagId(dsl, ragId);
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(DSLContext dsl, String docId) {
        return ragDocDao.deleteByDocId(dsl, docId);
    }

    /**
     * 删除特定的RAG-文档关联
     */
    public int delete(DSLContext dsl, Long ragId, String docId) {
        return ragDocDao.delete(dsl, ragId, docId);
    }
}

