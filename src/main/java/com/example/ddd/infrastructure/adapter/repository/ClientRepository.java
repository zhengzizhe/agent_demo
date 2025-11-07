package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IChatModelRepository;
import com.example.ddd.domain.agent.adapter.repository.IClientRepository;
import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.infrastructure.dao.IClientDao;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

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
        // 先查询agent关联的client IDs
        List<Long> clientIds = dslContext.select(field("client_id"))
                .from(table("agent_client"))
                .where(field("agent_id").eq(agentId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("client_id"))
                .collect(Collectors.toList());

        if (clientIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 根据client IDs查询client列表
        return dslContext.select()
                .from(table("client"))
                .where(field("client.id").in(clientIds))
                .fetch()
                .stream()
                .map(record -> {
                    ClientEntity entity = new ClientEntity();
                    entity.setId((Long) record.get("id"));
                    entity.setName((String) record.get("name"));
                    entity.setDescription((String) record.get("description"));
                    entity.setStatus((String) record.get("status"));
                    entity.setCreatedAt((Long) record.get("created_at"));
                    entity.setUpdatedAt((Long) record.get("updated_at"));
                    entity.setSystemPrompt((String) record.get("system_prompt"));
                    return entity;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ClientEntity queryById(DSLContext dslContext, Long id) {
        var record = dslContext.select()
                .from(table("client"))
                .where(field("client.id").eq(id))
                .fetchOne();

        if (record == null) {
            return null;
        }

        ClientEntity entity = new ClientEntity();
        entity.setId((Long) record.get("id"));
        entity.setName((String) record.get("name"));
        entity.setDescription((String) record.get("description"));
        entity.setStatus((String) record.get("status"));
        entity.setCreatedAt((Long) record.get("created_at"));
        entity.setUpdatedAt((Long) record.get("updated_at"));
        entity.setSystemPrompt((String) record.get("system_prompt"));
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

