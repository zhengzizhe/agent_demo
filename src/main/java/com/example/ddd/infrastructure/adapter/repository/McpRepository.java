package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IMcpRepository;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.infrastructure.dao.IMcpDao;
import com.example.ddd.infrastructure.dao.po.McpPO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mcp仓储实现
 */
@Singleton
public class McpRepository implements IMcpRepository {

    @Inject
    private IMcpDao mcpDao;

    @Override
    public McpEntity insert(DSLContext dslContext, McpEntity mcpEntity) {
        McpPO mcpPO = convertToPO(mcpEntity);
        McpPO result = mcpDao.insert(dslContext, mcpPO);
        return convertToEntity(result);
    }

    @Override
    public McpEntity queryById(DSLContext dslContext, Long id) {
        McpPO po = mcpDao.queryById(dslContext, id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<McpEntity> queryAll(DSLContext dslContext) {
        return mcpDao.queryAll(dslContext).stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(DSLContext dslContext, McpEntity mcpEntity) {
        McpPO mcpPO = convertToPO(mcpEntity);
        return mcpDao.update(dslContext, mcpPO);
    }

    @Override
    public int deleteById(DSLContext dslContext, Long id) {
        return mcpDao.deleteById(dslContext, id);
    }

    // 转换方法
    private McpPO convertToPO(McpEntity entity) {
        if (entity == null) return null;
        McpPO po = new McpPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setType(entity.getType());
        po.setEndpoint(entity.getEndpoint());
        po.setConfig(entity.getConfig() != null ? org.jooq.JSONB.valueOf(entity.getConfig().toString()) : null);
        po.setStatus(entity.getStatus());
        return po;
    }

    private McpEntity convertToEntity(McpPO po) {
        if (po == null) return null;
        McpEntity entity = new McpEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setType(po.getType());
        entity.setEndpoint(po.getEndpoint());
        entity.setConfig(po.getConfig());
        entity.setStatus(po.getStatus());
        return entity;
    }
}
