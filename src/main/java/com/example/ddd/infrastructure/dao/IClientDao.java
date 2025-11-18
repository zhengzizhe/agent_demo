package com.example.ddd.infrastructure.dao;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

import static com.example.jooq.tables.AgentModel.AGENT_MODEL;
import static com.example.jooq.tables.AgentRag.AGENT_RAG;
import static com.example.jooq.tables.AgentMcp.AGENT_MCP;

/**
 * Client相关DAO
 */
@Singleton
public class IClientDao {

    /**
     * 根据Agent ID查询关联的Model ID列表
     */
    public List<Long> queryModelIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(AGENT_MODEL.MODEL_ID)
                .from(AGENT_MODEL)
                .where(AGENT_MODEL.AGENT_ID.eq(clientId))
                .fetchInto(Long.class);
    }

    /**
     * 根据Agent ID查询关联的RAG ID列表
     */
    public List<Long> queryRagIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(AGENT_RAG.RAG_ID)
                .from(AGENT_RAG)
                .where(AGENT_RAG.AGENT_ID.eq(clientId))
                .fetchInto(Long.class);
    }

    /**
     * 根据Agent ID查询关联的MCP ID列表
     */
    public List<Long> queryMcpIdsByClientId(DSLContext dslContext, Long clientId) {
        return dslContext.select(AGENT_MCP.MCP_ID)
                .from(AGENT_MCP)
                .where(AGENT_MCP.AGENT_ID.eq(clientId))
                .fetchInto(Long.class);
    }
}
















