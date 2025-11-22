package com.example.ddd.domain.agent.repository;

import com.example.ddd.infrastructure.dao.mapper.RagDocMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * RAG-文档关联仓储
 */
@Repository
public class RagDocRepository {
    
    @Autowired
    private RagDocMapper ragDocMapper;

    /**
     * 插入RAG-文档关联
     */
    public void insert(String ragId, String docId) {
        ragDocMapper.insert(ragId, docId);
    }

    /**
     * 根据RAG ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByRagId(String ragId) {
        return ragDocMapper.queryDocIdsByRagId(ragId);
    }

    /**
     * 根据文档ID查询关联的RAG ID列表
     */
    public List<String> queryRagIdsByDocId(String docId) {
        return ragDocMapper.queryRagIdsByDocId(docId);
    }

    /**
     * 删除RAG的所有关联
     */
    public int deleteByRagId(String ragId) {
        return ragDocMapper.deleteByRagId(ragId);
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(String docId) {
        return ragDocMapper.deleteByDocId(docId);
    }

    /**
     * 删除特定的RAG-文档关联
     */
    public int delete(String ragId, String docId) {
        return ragDocMapper.delete(ragId, docId);
    }
}
