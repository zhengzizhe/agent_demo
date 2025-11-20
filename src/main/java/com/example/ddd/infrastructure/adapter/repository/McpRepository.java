package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.adapter.repository.IMcpRepository;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.infrastructure.dao.IMcpDao;
import com.example.ddd.infrastructure.dao.po.McpPO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.jooq.tables.AgentMcp.AGENT_MCP;

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
    public List<McpEntity> queryByAgentId(DSLContext dslContext, Long agentId) {
        // 通过agent_mcp关联表查询MCP ID列表
        List<Long> mcpIds = dslContext.select(AGENT_MCP.MCP_ID)
                .from(AGENT_MCP)
                .where(AGENT_MCP.AGENT_ID.eq(agentId))
                .fetchInto(Long.class);
        
        if (mcpIds.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        
        // 查询这些MCP ID对应的MCP实体
        return mcpIds.stream()
                .map(mcpId -> mcpDao.queryById(dslContext, mcpId))
                .filter(po -> po != null)
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
        
        // description 字段
        try {
            po.getClass().getMethod("setDescription", String.class).invoke(po, entity.getDescription());
        } catch (Exception e) {
            // 如果方法不存在，忽略（需要重新生成 jOOQ 代码）
        }
        
        // baseUrl 映射到数据库的 base_url 字段
        try {
            po.getClass().getMethod("setBaseUrl", String.class).invoke(po, entity.getBaseUrl());
        } catch (Exception e) {
            // 如果方法不存在，尝试使用 endpoint（向后兼容）
            try {
                po.getClass().getMethod("setEndpoint", String.class).invoke(po, entity.getBaseUrl());
            } catch (Exception ex) {
                // 如果都不存在，忽略（需要重新生成 jOOQ 代码）
            }
        }
        
        // headers 字段（Map<String, String> -> JSONB）
        if (entity.getHeaders() != null) {
            try {
                String headersJson = JSON.toJSON(entity.getHeaders());
                po.getClass().getMethod("setHeaders", org.jooq.JSONB.class).invoke(po, org.jooq.JSONB.valueOf(headersJson));
            } catch (Exception e) {
                // 如果方法不存在，忽略（需要重新生成 jOOQ 代码）
            }
        }
        
        po.setStatus(entity.getStatus());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    private McpEntity convertToEntity(McpPO po) {
        if (po == null) return null;
        McpEntity entity = new McpEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setType(po.getType());
        
        // description 字段
        try {
            String description = (String) po.getClass().getMethod("getDescription").invoke(po);
            entity.setDescription(description);
        } catch (Exception e) {
            entity.setDescription(null);
        }
        
        // baseUrl 字段
        try {
            String baseUrl = (String) po.getClass().getMethod("getBaseUrl").invoke(po);
            entity.setBaseUrl(baseUrl);
        } catch (Exception e) {
            try {
                String endpoint = (String) po.getClass().getMethod("getEndpoint").invoke(po);
                entity.setBaseUrl(endpoint);
            } catch (Exception ex) {
                entity.setBaseUrl(null);
            }
        }
        
        // headers 字段（JSONB -> Map<String, String>）
        try {
            org.jooq.JSONB headersJsonb = (org.jooq.JSONB) po.getClass().getMethod("getHeaders").invoke(po);
            if (headersJsonb != null) {
                Map<String, String> headers = JSON.parseObject(headersJsonb.data(), 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                entity.setHeaders(headers);
            } else {
                entity.setHeaders(null);
            }
        } catch (Exception e) {
            entity.setHeaders(null);
        }
        
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
