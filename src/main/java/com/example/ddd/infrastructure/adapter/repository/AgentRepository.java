package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.domain.agent.adapter.repository.IAgentRepository;
import com.example.ddd.domain.agent.adapter.repository.IChatModelRepository;
import com.example.ddd.domain.agent.adapter.repository.IMcpRepository;
import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.model.entity.AgentEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.domain.agent.service.execute.role.AgentRole;
import com.example.ddd.infrastructure.dao.IClientDao;
import com.example.ddd.infrastructure.dao.po.AgentPO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;
import static com.example.jooq.tables.Agent.AGENT;

/**
 * Agent仓储实现（原Client仓储）
 */
@Singleton
public class AgentRepository implements IAgentRepository {

    @Inject
    private IClientDao clientDao;
    @Inject
    private IChatModelRepository chatModelRepository;
    @Inject
    private IRagRepository ragRepository;
    @Inject
    private IMcpRepository mcpRepository;

    @Override
    public List<AgentEntity> queryByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
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

        // 查询这些agent IDs对应的Agent实体，同时查询role和system_prompt
        List<AgentEntity> agents = new ArrayList<>();
        for (Long agentId : agentIds) {
            // 使用 jOOQ 生成的表，JOIN orchestrator_agent 表获取 role
            var result = dslContext.select(
                    AGENT.ID,
                    AGENT.NAME,
                    AGENT.DESCRIPTION,
                    AGENT.STATUS,
                    AGENT.CREATED_AT,
                    AGENT.UPDATED_AT,
                    field("system_prompt", String.class),
                    field("role", Long.class).as("agent_role")
            )
            .from(AGENT)
            .leftJoin(table("orchestrator_agent"))
                .on(field("orchestrator_agent.agent_id").eq(AGENT.ID))
                .and(field("orchestrator_agent.orchestrator_id").eq(orchestratorId))
            .where(AGENT.ID.eq(agentId))
            .fetchOne();
            
            if (result != null) {
                AgentPO agentPO = new AgentPO();
                agentPO.setId(result.get(AGENT.ID));
                agentPO.setName(result.get(AGENT.NAME));
                agentPO.setDescription(result.get(AGENT.DESCRIPTION));
                agentPO.setStatus(result.get(AGENT.STATUS));
                agentPO.setCreatedAt(result.get(AGENT.CREATED_AT));
                agentPO.setUpdatedAt(result.get(AGENT.UPDATED_AT));
                
                AgentEntity entity = convertToEntity(agentPO);
                entity.setOrchestratorId(orchestratorId);
                // 设置 system_prompt（如果 jOOQ 已生成该字段，否则从 result 获取）
                String systemPrompt = result.get("system_prompt", String.class);
                if (systemPrompt != null) {
                    entity.setSystemPrompt(systemPrompt);
                }
                // 设置 role
                Long roleCode = result.get("agent_role", Long.class);
                if (roleCode != null) {
                    entity.setRole(AgentRole.by(roleCode));
                }
                agents.add(entity);
            }
        }

        return agents;
    }

    @Override
    public Map<Long, List<ChatModelEntity>> queryModelMapByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
        // 先查询orchestrator关联的agent IDs
        List<Long> agentIds = dslContext.select(field("agent_id"))
                .from(table("orchestrator_agent"))
                .where(field("orchestrator_id").eq(orchestratorId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("agent_id"))
                .collect(Collectors.toList());

        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 为每个agent查询其models
        Map<Long, List<ChatModelEntity>> modelMap = new HashMap<>();
        for (Long agentId : agentIds) {
            List<ChatModelEntity> models = chatModelRepository.queryByAgentId(dslContext, agentId);
            if (!models.isEmpty()) {
                modelMap.put(agentId, models);
            }
        }

        return modelMap;
    }

    @Override
    public Map<Long, List<RagEntity>> queryRagMapByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
        // 先查询orchestrator关联的agent IDs
        List<Long> agentIds = dslContext.select(field("agent_id"))
                .from(table("orchestrator_agent"))
                .where(field("orchestrator_id").eq(orchestratorId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("agent_id"))
                .collect(Collectors.toList());

        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 为每个agent查询其rags
        Map<Long, List<RagEntity>> ragMap = new HashMap<>();
        for (Long agentId : agentIds) {
            List<RagEntity> rags = ragRepository.queryByAgentId(dslContext, agentId);
            if (!rags.isEmpty()) {
                ragMap.put(agentId, rags);
            }
        }

        return ragMap;
    }

    @Override
    public Map<Long, List<McpEntity>> queryMcpMapByOrchestratorId(DSLContext dslContext, Long orchestratorId) {
        // 先查询orchestrator关联的agent IDs
        List<Long> agentIds = dslContext.select(field("agent_id"))
                .from(table("orchestrator_agent"))
                .where(field("orchestrator_id").eq(orchestratorId))
                .fetch()
                .stream()
                .map(record -> (Long) record.get("agent_id"))
                .collect(Collectors.toList());

        if (agentIds.isEmpty()) {
            return new HashMap<>();
        }

        // 为每个agent查询其mcps
        Map<Long, List<McpEntity>> mcpMap = new HashMap<>();
        for (Long agentId : agentIds) {
            List<McpEntity> mcps = mcpRepository.queryByAgentId(dslContext, agentId);
            if (!mcps.isEmpty()) {
                mcpMap.put(agentId, mcps);
            }
        }

        return mcpMap;
    }




    // 转换方法
    private AgentEntity convertToEntity(AgentPO po) {
        if (po == null) return null;
        AgentEntity entity = new AgentEntity();
        entity.setId(po.getId());
        entity.setName(po.getName());
        entity.setDescription(po.getDescription());
        entity.setStatus(po.getStatus());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        // 注意：systemPrompt 和 role 需要从数据库查询，这里暂时不设置
        return entity;
    }
}

