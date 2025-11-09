package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.RagPO;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;

import static com.example.jooq.tables.Rag.RAG;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;

/**
 * RAG数据访问对象
 */
@Singleton
public class IRagDao {

    /**
     * 插入RAG
     */
    public RagPO insert(DSLContext dslContext, RagPO ragPO) {
        return dslContext.insertInto(RAG)
                .set(RAG.NAME, ragPO.getName())
                .set(RAG.VECTOR_STORE_TYPE, ragPO.getVectorStoreType())
                .set(RAG.EMBEDDING_MODEL, ragPO.getEmbeddingModel())
                .set(RAG.CHUNK_SIZE, ragPO.getChunkSize())
                .set(RAG.CHUNK_OVERLAP, ragPO.getChunkOverlap())
                .set(RAG.CONFIG, ragPO.getConfig())
                .set(RAG.STATUS, ragPO.getStatus())
                .returning()
                .fetchOneInto(RagPO.class);
    }

    /**
     * 根据ID查询RAG
     */
    public RagPO queryById(DSLContext dslContext, Long id) {
        return dslContext.selectFrom(RAG)
                .where(RAG.ID.eq(id))
                .fetchOneInto(RagPO.class);
    }

    /**
     * 根据名称查询RAG
     */
    public RagPO queryByName(DSLContext dslContext, String name) {
        return dslContext.selectFrom(RAG)
                .where(RAG.NAME.eq(name))
                .fetchOneInto(RagPO.class);
    }

    /**
     * 根据向量存储类型查询RAG列表
     */
    public List<RagPO> queryByVectorStoreType(DSLContext dslContext, String vectorStoreType) {
        return dslContext.selectFrom(RAG)
                .where(RAG.VECTOR_STORE_TYPE.eq(vectorStoreType))
                .fetchInto(RagPO.class);
    }

    /**
     * 查询所有RAG
     */
    public List<RagPO> queryAll(DSLContext dslContext) {
        return dslContext.selectFrom(RAG)
                .fetchInto(RagPO.class);
    }

    /**
     * 根据状态查询RAG列表
     */
    public List<RagPO> queryByStatus(DSLContext dslContext, String status) {
        return dslContext.selectFrom(RAG)
                .where(RAG.STATUS.eq(status))
                .fetchInto(RagPO.class);
    }

    /**
     * 更新RAG
     */
    public int update(DSLContext dslContext, RagPO ragPO) {
        return dslContext.update(RAG)
                .set(RAG.NAME, ragPO.getName())
                .set(RAG.VECTOR_STORE_TYPE, ragPO.getVectorStoreType())
                .set(RAG.EMBEDDING_MODEL, ragPO.getEmbeddingModel())
                .set(RAG.CHUNK_SIZE, ragPO.getChunkSize())
                .set(RAG.CHUNK_OVERLAP, ragPO.getChunkOverlap())
                .set(RAG.CONFIG, ragPO.getConfig())
                .set(RAG.STATUS, ragPO.getStatus())
                .set(RAG.UPDATED_AT, ragPO.getUpdatedAt())
                .where(RAG.ID.eq(ragPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除RAG
     */
    public int deleteById(DSLContext dslContext, Long id) {
        return dslContext.deleteFrom(RAG)
                .where(RAG.ID.eq(id))
                .execute();
    }

    /**
     * 根据Client ID列表查询RAG列表（通过关联表）
     */
    public List<RagPO> queryByClientIds(DSLContext dslContext, List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 先通过 client_rag 关联表查询 rag IDs
        List<Long> ragIds = dslContext.select(field("rag_id"))
                .from(table("client_rag"))
                .where(field("client_id").in(clientIds))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("rag_id"))
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        if (ragIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 rag IDs 对应的 RAG
        return dslContext.selectFrom(RAG)
                .where(RAG.ID.in(ragIds))
                .fetchInto(RagPO.class);
    }

    /**
     * 根据Client ID查询RAG列表（通过关联表）
     */
    public List<RagPO> queryByClientId(DSLContext dslContext, Long clientId) {
        // 先通过 client_rag 关联表查询 rag IDs
        List<Long> ragIds = dslContext.select(field("rag_id"))
                .from(table("client_rag"))
                .where(field("client_id").eq(clientId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("rag_id"))
                .collect(java.util.stream.Collectors.toList());
        
        if (ragIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 rag IDs 对应的 RAG
        return dslContext.selectFrom(RAG)
                .where(RAG.ID.in(ragIds))
                .fetchInto(RagPO.class);
    }

    /**
     * 根据Agent ID查询关联的RAG列表
     * 先查询agent关联的client IDs，然后通过clientId查询RAG
     */
    public List<RagPO> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 先查询agent关联的client IDs
        List<Long> clientIds = dslContext.select(field("client_id"))
                .from(table("agent_client"))
                .where(field("agent_id").eq(agentId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("client_id"))
                .collect(java.util.stream.Collectors.toList());
        
        if (clientIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 通过clientId查询RAG
        return queryByClientIds(dslContext, clientIds);
    }

    /**
     * 批量删除RAG
     */
    public int deleteByIds(DSLContext dslContext, List<Long> ids) {
        return dslContext.deleteFrom(RAG)
                .where(RAG.ID.in(ids))
                .execute();
    }
}

