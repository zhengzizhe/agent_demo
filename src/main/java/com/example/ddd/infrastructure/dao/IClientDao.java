package com.example.ddd.infrastructure.dao;


import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.DSLContext;

/**
 * Client相关DAO
 */
@Singleton
public class IClientDao {

    /**
     * 根据Agent ID查询关联的Model ID列表
     */
    public List<Long> queryModelIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(field("model_id"))
                .from(table("agent_model"))
                .where(field("agent_id").eq(clientId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("model_id"))
                .collect(Collectors.toList());
    }

    /**
     * 根据Agent ID查询关联的RAG ID列表
     */
    public List<Long> queryRagIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(field("rag_id"))
                .from(table("agent_rag"))
                .where(field("agent_id").eq(clientId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("rag_id"))
                .collect(Collectors.toList());
    }

    /**
     * 根据Agent ID查询关联的MCP ID列表
     */
    public List<Long> queryMcpIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(field("mcp_id"))
                .from(table("agent_mcp"))
                .where(field("agent_id").eq(clientId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("mcp_id"))
                .collect(Collectors.toList());
    }
}
















