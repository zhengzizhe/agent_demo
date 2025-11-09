package com.example.ddd.infrastructure.adapter.repository;


import com.example.ddd.domain.agent.adapter.repository.IChatModelRepository;
import com.example.ddd.domain.agent.adapter.repository.IClientRepository;
import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.infrastructure.dao.IClientDao;
import com.example.jooq.Tables;
import com.example.jooq.tables.records.AgentClientRecord;
import com.example.jooq.tables.records.ClientRecord;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Client仓储实现
 */
@Singleton
public class ClientRepository implements IClientRepository {

    @Inject
    private IClientDao clientDao;

    @Inject
    private IChatModelRepository chatModelRepository;

    @Inject
    private IRagRepository ragRepository;

    @Override
    public List<ClientEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 1. 先查询 agent_client 关联表，获取 client_id 和 seq（顺序）
        List<AgentClientRecord> agentClientRecords = dslContext
                .selectFrom(Tables.AGENT_CLIENT)
                .where(Tables.AGENT_CLIENT.AGENT_ID.eq(agentId))
                .orderBy(Tables.AGENT_CLIENT.SEQ.asc().nullsLast(), Tables.AGENT_CLIENT.CLIENT_ID.asc())
                .fetch();

        if (agentClientRecords.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 提取 client_id 列表
        List<Long> clientIds = agentClientRecords.stream()
                .map(AgentClientRecord::getClientId)
                .collect(Collectors.toList());

        // 3. 通过 client_ids 查询 client 表
        Map<Long, ClientRecord> clientRecordMap = dslContext
                .selectFrom(Tables.CLIENT)
                .where(Tables.CLIENT.ID.in(clientIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(ClientRecord::getId, record -> record));

        // 4. 按照 agent_client 中的顺序构建 ClientEntity 列表
        return agentClientRecords.stream()
                .map(agentClientRecord -> {
                    Long clientId = agentClientRecord.getClientId();
                    ClientRecord clientRecord = clientRecordMap.get(clientId);
                    if (clientRecord == null) {
                        return null;
                    }
                    // 将 ClientRecord 转换为 ClientEntity
                    ClientEntity entity = new ClientEntity();
                    entity.setId(clientRecord.getId());
                    entity.setName(clientRecord.getName());
                    entity.setDescription(clientRecord.getDescription());
                    entity.setStatus(clientRecord.getStatus());
                    entity.setCreatedAt(clientRecord.getCreatedAt());
                    entity.setUpdatedAt(clientRecord.getUpdatedAt());
                    entity.setSystemPrompt(clientRecord.getSystemPrompt());
                    return entity;
                })
                .filter(entity -> entity != null)
                .collect(Collectors.toList());
    }

    @Override
    public ClientEntity queryById(DSLContext dslContext, Long id) {
        ClientRecord record = dslContext
                .selectFrom(Tables.CLIENT)
                .where(Tables.CLIENT.ID.eq(id))
                .fetchOne();

        if (record == null) {
            return null;
        }

        // 将 ClientRecord 转换为 ClientEntity
        ClientEntity entity = new ClientEntity();
        entity.setId(record.getId());
        entity.setName(record.getName());
        entity.setDescription(record.getDescription());
        entity.setStatus(record.getStatus());
        entity.setCreatedAt(record.getCreatedAt());
        entity.setUpdatedAt(record.getUpdatedAt());
        entity.setSystemPrompt(record.getSystemPrompt());
        return entity;
    }

    @Override
    public List<ChatModelEntity> queryModelsByClientId(DSLContext dslContext, Long clientId) {
        // 直接通过clientId查询Model
        return chatModelRepository.queryByClientId(dslContext, clientId);
    }

    @Override
    public List<RagEntity> queryRagsByClientId(DSLContext dslContext, Long clientId) {
        // 直接通过clientId查询RAG
        return ragRepository.queryByClientId(dslContext, clientId);
    }

    @Override
    public List<Long> queryMcpIdsByClientId(DSLContext dslContext, Long clientId) {
        return clientDao.queryMcpIdsByClientId(dslContext, clientId);
    }

    @Override
    public Map<Long, Long> buildModelToRagMap(DSLContext dslContext, List<ClientEntity> clients) {
        Map<Long, Long> map = new HashMap<>();
        for (ClientEntity client : clients) {
            List<Long> modelIds = clientDao.queryModelIdsByClientId(dslContext, client.getId());
            List<Long> ragIds = clientDao.queryRagIdsByClientId(dslContext, client.getId());

            // 假设 model 和 rag 在 client 中是一对一关系（按顺序匹配）
            if (modelIds.size() == ragIds.size()) {
                for (int i = 0; i < modelIds.size(); i++) {
                    map.put(modelIds.get(i), ragIds.get(i));
                }
            }
        }
        return map;
    }

    @Override
    public Map<Long, List<ChatModelEntity>> queryModelMapByAgentId(DSLContext dslContext, Long agentId) {
        List<ClientEntity> clients = queryByAgentId(dslContext, agentId);
        return clients.stream()
                .collect(Collectors.toMap(
                        ClientEntity::getId,
                        client -> queryModelsByClientId(dslContext, client.getId())
                ));
    }

    @Override
    public Map<Long, List<RagEntity>> queryRagMapByAgentId(DSLContext dslContext, Long agentId) {
        List<ClientEntity> clients = queryByAgentId(dslContext, agentId);
        return clients.stream()
                .collect(Collectors.toMap(
                        ClientEntity::getId,
                        client -> queryRagsByClientId(dslContext, client.getId())
                ));
    }
}

