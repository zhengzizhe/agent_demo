package com.example.ddd.domain.agent.repository;

import com.example.ddd.application.agent.repository.IMcpRepository;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.infrastructure.dao.mapper.McpMapper;
import com.example.ddd.infrastructure.dao.po.McpPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mcp仓储实现
 */
@Repository
public class McpRepository implements IMcpRepository {

    @Autowired
    private McpMapper mcpMapper;

    @Override
    public McpEntity insert(McpEntity mcpEntity) {
        McpPO mcpPO = convertToPO(mcpEntity);
        mcpMapper.insert(mcpPO);
        return convertToEntity(mcpPO);
    }

    @Override
    public McpEntity queryById(String id) {
        McpPO po = mcpMapper.queryById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<McpEntity> queryAll() {
        return mcpMapper.queryAll().stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<McpEntity> queryByAgentId(String agentId) {
        List<McpPO> pos = mcpMapper.queryByAgentId(agentId);
        return pos.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public int update(McpEntity mcpEntity) {
        McpPO mcpPO = convertToPO(mcpEntity);
        return mcpMapper.update(mcpPO);
    }

    @Override
    public int deleteById(String id) {
        return mcpMapper.deleteById(id);
    }

    // 转换方法
    private McpPO convertToPO(McpEntity entity) {
        if (entity == null) return null;
        McpPO po = new McpPO();
        po.setId(entity.getId());
        po.setName(entity.getName());
        po.setType(entity.getType());
        po.setDescription(entity.getDescription());
        po.setBaseUrl(entity.getBaseUrl());
        
        // headers 字段（Map<String, String> -> JSON字符串）
        if (entity.getHeaders() != null) {
            String headersJson = JSON.toJSON(entity.getHeaders());
            po.setHeaders(headersJson);
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
        entity.setDescription(po.getDescription());
        entity.setBaseUrl(po.getBaseUrl());
        
        // headers 字段（JSON字符串 -> Map<String, String>）
        if (po.getHeaders() != null) {
            try {
                Map<String, String> headers = JSON.parseObject(po.getHeaders(), 
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                entity.setHeaders(headers);
            } catch (Exception e) {
                entity.setHeaders(null);
            }
        } else {
            entity.setHeaders(null);
        }
        
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}
