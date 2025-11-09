package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IAgentRepository;
import com.example.ddd.domain.agent.model.entity.AgentEntity;
import com.example.ddd.infrastructure.dao.IAgentDao;
import com.example.ddd.infrastructure.dao.po.AgentPO;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent仓储实现
 */
@Singleton
public class AgentRepository implements IAgentRepository {

    @Inject
    private IAgentDao agentDao;

    @Override
    public AgentEntity insert(DSLContext dslContext, AgentEntity agentEntity) {
        AgentPO agentPO = convertToPO(agentEntity);
        AgentPO result = agentDao.insert(dslContext, agentPO);
        return convertToEntity(result);
    }

    @Override
    public AgentEntity queryById(DSLContext dslContext, Long id) {
        AgentPO po = agentDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<AgentEntity> queryAll(DSLContext dslContext) {
        return agentDao.queryAll(dslContext).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(DSLContext dslContext, AgentEntity agentEntity) {
        AgentPO agentPO = convertToPO(agentEntity);
        return agentDao.update(dslContext, agentPO);
    }

    @Override
    public int deleteById(DSLContext dslContext, Long id) {
        return agentDao.deleteById(dslContext, id);
    }

    // 转换方法
    private AgentPO convertToPO(AgentEntity entity) {
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

    private AgentEntity convertToEntity(AgentPO po) {
        if (po == null) return null;
        AgentEntity entity = new AgentEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
