package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IOrchestratorRepository;
import com.example.ddd.domain.agent.model.entity.OrchestratorEntity;
import com.example.ddd.infrastructure.dao.IAgentDao;
import com.example.ddd.infrastructure.dao.po.AgentPO;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrator仓储实现（原Agent仓储）
 */
@Singleton
public class OrchestratorRepository implements IOrchestratorRepository {

    @Inject
    private IAgentDao agentDao;

    @Override
    public OrchestratorEntity insert(DSLContext dslContext, OrchestratorEntity orchestratorEntity) {
        AgentPO agentPO = convertToPO(orchestratorEntity);
        AgentPO result = agentDao.insert(dslContext, agentPO);
        return convertToEntity(result);
    }

    @Override
    public OrchestratorEntity queryById(DSLContext dslContext, Long id) {
        AgentPO po = agentDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<OrchestratorEntity> queryAll(DSLContext dslContext) {
        return agentDao.queryAll(dslContext).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(DSLContext dslContext, OrchestratorEntity orchestratorEntity) {
        AgentPO agentPO = convertToPO(orchestratorEntity);
        return agentDao.update(dslContext, agentPO);
    }

    @Override
    public int deleteById(DSLContext dslContext, Long id) {
        return agentDao.deleteById(dslContext, id);
    }

    // 转换方法
    private AgentPO convertToPO(OrchestratorEntity entity) {
        if (entity == null) return null;
        AgentPO po = new AgentPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setDescription(entity.getDescription());
        po.setStatus(entity.getStatus());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    private OrchestratorEntity convertToEntity(AgentPO po) {
        if (po == null) return null;
        OrchestratorEntity entity = new OrchestratorEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}

