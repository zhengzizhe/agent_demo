package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IChatModelRepository;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.infrastructure.dao.IModelDao;
import com.example.ddd.infrastructure.dao.po.ModelPO;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;

/**
 * ChatModel仓储实现
 */
@Singleton
public class ChatModelRepository implements IChatModelRepository {

    @Inject
    private IModelDao modelDao;

    @Override
    public ChatModelEntity insert(DSLContext dslContext, ChatModelEntity chatModelEntity) {
        ModelPO modelPO = convertToPO(chatModelEntity);
        ModelPO result = modelDao.insert(dslContext, modelPO);
        return convertToEntity(result);
    }

    @Override
    public ChatModelEntity queryById(DSLContext dslContext, Long id) {
        ModelPO po = modelDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<ChatModelEntity> queryAll(DSLContext dslContext) {
        return modelDao.queryAll(dslContext).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(DSLContext dslContext, ChatModelEntity chatModelEntity) {
        ModelPO modelPO = convertToPO(chatModelEntity);
        return modelDao.update(dslContext, modelPO);
    }

    @Override
    public int deleteById(DSLContext dslContext, Long id) {
        return modelDao.deleteById(dslContext, id);
    }

    /**
     * 根据Agent ID查询关联的ChatModel列表
     * 先查询agent关联的client IDs，然后通过clientId查询Model
     */
    @Override
    public List<ChatModelEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
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
        
        // 通过clientId查询Model，并建立 modelId -> clientId 的映射
        Map<Long, Long> modelToClientMap = new java.util.HashMap<>();
        for (Long clientId : clientIds) {
            List<Long> modelIds = dslContext.select(field("model_id"))
                    .from(table("client_model"))
                    .where(field("client_id").eq(clientId))
                    .fetch()
                    .stream()
                    .map(record -> (Long) record.get("model_id"))
                    .collect(Collectors.toList());
            for (Long modelId : modelIds) {
                modelToClientMap.putIfAbsent(modelId, clientId);
            }
        }
        
        // 查询这些 model IDs 对应的 Model
        List<Long> allModelIds = new ArrayList<>(modelToClientMap.keySet());
        if (allModelIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return modelDao.queryByClientIds(dslContext, clientIds).stream()
                .map(po -> {
                    ChatModelEntity entity = convertToEntity(po);
                    // 设置 clientId
                    if (entity != null && modelToClientMap.containsKey(entity.getId())) {
                        entity.setClientId(modelToClientMap.get(entity.getId()));
                    }
                    return entity;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据Client ID查询Model列表
     */
    public List<ChatModelEntity> queryByClientId(DSLContext dslContext, Long clientId) {
        return modelDao.queryByClientId(dslContext, clientId).stream()
                .map(po -> {
                    ChatModelEntity entity = convertToEntity(po);
                    // 设置 clientId
                    if (entity != null) {
                        entity.setClientId(clientId);
                    }
                    return entity;
                })
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
