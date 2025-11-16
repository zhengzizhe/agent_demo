package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.ModelPO;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.List;

import static com.example.jooq.tables.Model.MODEL;
import static org.jooq.impl.DSL.table;
import static org.jooq.impl.DSL.field;

/**
 * Model数据访问对象
 */
@Singleton
public class IModelDao {

    /**
     * 插入Model
     */
    public ModelPO insert(DSLContext dslContext, ModelPO modelPO) {
        return dslContext.insertInto(MODEL)
                .set(MODEL.NAME, modelPO.getName())
                .set(MODEL.PROVIDER, modelPO.getProvider())
                .set(MODEL.MODEL_TYPE, modelPO.getModelType())
                .set(MODEL.API_ENDPOINT, modelPO.getApiEndpoint())
                .set(MODEL.API_KEY, modelPO.getApiKey())
                .set(MODEL.MAX_TOKENS, modelPO.getMaxTokens())
                .set(MODEL.TEMPERATURE, modelPO.getTemperature())
                .set(MODEL.STATUS, modelPO.getStatus())
                .returning()
                .fetchOneInto(ModelPO.class);
    }

    /**
     * 根据ID查询Model
     */
    public ModelPO queryById(DSLContext dslContext, Long id) {
        return dslContext.selectFrom(MODEL)
                .where(MODEL.ID.eq(id))
                .fetchOneInto(ModelPO.class);
    }

    /**
     * 根据名称查询Model
     */
    public ModelPO queryByName(DSLContext dslContext, String name) {
        return dslContext.selectFrom(MODEL)
                .where(MODEL.NAME.eq(name))
                .fetchOneInto(ModelPO.class);
    }

    /**
     * 根据提供商查询Model列表
     */
    public List<ModelPO> queryByProvider(DSLContext dslContext, String provider) {
        return dslContext.selectFrom(MODEL)
                .where(MODEL.PROVIDER.eq(provider))
                .fetchInto(ModelPO.class);
    }

    /**
     * 查询所有Model
     */
    public List<ModelPO> queryAll(DSLContext dslContext) {
        return dslContext.selectFrom(MODEL)
                .fetchInto(ModelPO.class);
    }

    /**
     * 根据状态查询Model列表
     */
    public List<ModelPO> queryByStatus(DSLContext dslContext, String status) {
        return dslContext.selectFrom(MODEL)
                .where(MODEL.STATUS.eq(status))
                .fetchInto(ModelPO.class);
    }

    /**
     * 更新Model
     */
    public int update(DSLContext dslContext, ModelPO modelPO) {
        return dslContext.update(MODEL)
                .set(MODEL.NAME, modelPO.getName())
                .set(MODEL.PROVIDER, modelPO.getProvider())
                .set(MODEL.MODEL_TYPE, modelPO.getModelType())
                .set(MODEL.API_ENDPOINT, modelPO.getApiEndpoint())
                .set(MODEL.API_KEY, modelPO.getApiKey())
                .set(MODEL.MAX_TOKENS, modelPO.getMaxTokens())
                .set(MODEL.TEMPERATURE, modelPO.getTemperature())
                .set(MODEL.STATUS, modelPO.getStatus())
                .set(MODEL.UPDATED_AT, modelPO.getUpdatedAt())
                .where(MODEL.ID.eq(modelPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除Model
     */
    public int deleteById(DSLContext dslContext, Long id) {
        return dslContext.deleteFrom(MODEL)
                .where(MODEL.ID.eq(id))
                .execute();
    }

    /**
     * 根据Client ID列表查询Model列表（通过关联表）
     */
    public List<ModelPO> queryByClientIds(DSLContext dslContext, List<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return new ArrayList<>();
        }
        // 先通过 agent_model 关联表查询 model IDs
        List<Long> modelIds = dslContext.select(field("model_id"))
                .from(table("agent_model"))
                .where(field("agent_id").in(clientIds))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("model_id"))
                .distinct()
                .collect(java.util.stream.Collectors.toList());
        
        if (modelIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 model IDs 对应的 Model
        return dslContext.selectFrom(MODEL)
                .where(MODEL.ID.in(modelIds))
                .fetchInto(ModelPO.class);
    }

    /**
     * 根据Client ID查询Model列表（通过关联表）
     */
    public List<ModelPO> queryByClientId(DSLContext dslContext, Long clientId) {
        // 先通过 agent_model 关联表查询 model IDs
        List<Long> modelIds = dslContext.select(field("model_id"))
                .from(table("agent_model"))
                .where(field("agent_id").eq(clientId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("model_id"))
                .collect(java.util.stream.Collectors.toList());
        
        if (modelIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 查询这些 model IDs 对应的 Model
        return dslContext.selectFrom(MODEL)
                .where(MODEL.ID.in(modelIds))
                .fetchInto(ModelPO.class);
    }

    /**
     * 批量删除Model
     */
    public int deleteByIds(DSLContext dslContext, List<Long> ids) {
        return dslContext.deleteFrom(MODEL)
                .where(MODEL.ID.in(ids))
                .execute();
    }
}

