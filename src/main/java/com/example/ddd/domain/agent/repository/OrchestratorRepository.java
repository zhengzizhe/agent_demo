package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IOrchestratorRepository;
import com.example.ddd.domain.agent.model.entity.OrchestratorEntity;
import com.example.ddd.infrastructure.dao.mapper.AgentMapper;
import com.example.ddd.infrastructure.dao.po.AgentPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Orchestrator仓储实现（原Agent仓储）
 */
@Repository
public class OrchestratorRepository implements IOrchestratorRepository {

    @Autowired
    private AgentMapper agentMapper;

    @Override
    public OrchestratorEntity insert(OrchestratorEntity orchestratorEntity) {
        AgentPO agentPO = convertToPO(orchestratorEntity);
        agentMapper.insert(agentPO);
        return convertToEntity(agentPO);
    }

    @Override
    public OrchestratorEntity queryById(String id) {
        AgentPO po = agentMapper.queryById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<OrchestratorEntity> queryAll() {
        return agentMapper.queryAll().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(OrchestratorEntity orchestratorEntity) {
        AgentPO agentPO = convertToPO(orchestratorEntity);
        return agentMapper.update(agentPO);
    }

    @Override
    public int deleteById(String id) {
        return agentMapper.deleteById(id);
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
