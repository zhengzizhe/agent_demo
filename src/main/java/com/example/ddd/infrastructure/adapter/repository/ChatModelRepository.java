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
     * 直接通过agent_id查询agent_model关联表
     */
    @Override
    public List<ChatModelEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 直接通过agentId查询agent_model关联表
        List<Long> modelIds = dslContext.select(field("model_id"))
                .from(table("agent_model"))
                .where(field("agent_id").eq(agentId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("model_id"))
                .collect(Collectors.toList());
        
        if (modelIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 model IDs 对应的 Model
        return modelDao.queryByClientId(dslContext, agentId).stream()
                .map(po -> {
                    ChatModelEntity entity = convertToEntity(po);
                    // 设置 agentId
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
    public List<ChatModelEntity> queryByClientId(DSLContext dslContext, Long clientId) {
        return queryByAgentId(dslContext, clientId);
    }

    /**
     * 根据Orchestrator ID查询关联的ChatModel列表
     * 先查询orchestrator关联的agent IDs，然后查询这些agents的Model
     */
    @Override
    public List<ChatModelEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
        // 先查询orchestrator关联的agent IDs
        List<Long> agentIds = dslContext.select(field("agent_id"))
                .from(table("orchestrator_agent"))
                .where(field("orchestrator_id").eq(orchestratorId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("agent_id"))
                .collect(Collectors.toList());
        
        if (agentIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些agents的Model
        List<ChatModelEntity> allModels = new ArrayList<>();
        for (Long agentId : agentIds) {
            allModels.addAll(queryByAgentId(dslContext, agentId));
        }
        
        return allModels;
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
