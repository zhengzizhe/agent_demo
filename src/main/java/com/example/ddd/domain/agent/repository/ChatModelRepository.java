package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IChatModelRepository;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.infrastructure.dao.mapper.ModelMapper;
import com.example.ddd.infrastructure.dao.po.ModelPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ChatModel仓储实现
 */
@Repository
public class ChatModelRepository implements IChatModelRepository {

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ChatModelEntity insert(ChatModelEntity chatModelEntity) {
        ModelPO modelPO = convertToPO(chatModelEntity);
        modelMapper.insert(modelPO);
        return convertToEntity(modelPO);
    }

    @Override
    public ChatModelEntity queryById(String id) {
        ModelPO po = modelMapper.queryById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<ChatModelEntity> queryAll() {
        return modelMapper.queryAll().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(ChatModelEntity chatModelEntity) {
        ModelPO modelPO = convertToPO(chatModelEntity);
        return modelMapper.update(modelPO);
    }

    @Override
    public int deleteById(String id) {
        return modelMapper.deleteById(id);
    }

    /**
     * 根据Agent ID查询关联的ChatModel列表
     */
    @Override
    public List<ChatModelEntity> queryByAgentId(String agentId) {
        List<ModelPO> pos = modelMapper.queryByAgentId(agentId);
        return pos.stream()
                .map(po -> {
                    ChatModelEntity entity = convertToEntity(po);
                    if (entity != null) {
                        entity.setClientId(agentId);
                    }
                    return entity;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据Agent ID查询Model列表（兼容方法，实际调用queryByAgentId）
     */
    public List<ChatModelEntity> queryByClientId(String clientId) {
        return queryByAgentId(clientId);
    }

    /**
     * 根据Orchestrator ID查询关联的ChatModel列表
     */
    @Override
    public List<ChatModelEntity> queryByOrchestratorId(String orchestratorId) {
        List<ModelPO> pos = modelMapper.queryByOrchestratorId(orchestratorId);
        return pos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    // 转换方法
    private ModelPO convertToPO(ChatModelEntity entity) {
        if (entity == null) return null;
        ModelPO po = new ModelPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setProvider(entity.getProvider());
        po.setModelType(entity.getModelType());
        po.setApiEndpoint(entity.getApiEndpoint());
        po.setApiKey(entity.getApiKey());
        po.setMaxTokens(entity.getMaxTokens());
        po.setTemperature(entity.getTemperature());
        po.setStatus(entity.getStatus());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    private ChatModelEntity convertToEntity(ModelPO po) {
        if (po == null) return null;
        ChatModelEntity entity = new ChatModelEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setProvider(po.getProvider());
        entity.setModelType(po.getModelType());
        entity.setApiEndpoint(po.getApiEndpoint());
        entity.setApiKey(po.getApiKey());
        entity.setMaxTokens(po.getMaxTokens());
        entity.setTemperature(po.getTemperature());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
