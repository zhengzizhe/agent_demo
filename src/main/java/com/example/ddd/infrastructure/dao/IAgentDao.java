package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.AgentPO;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

import static com.example.jooq.tables.Agent.AGENT;

/**
 * Agent数据访问对象
 */
@Singleton
public class IAgentDao {

    /**
     * 插入Agent
     */
    public AgentPO insert(DSLContext dslContext, AgentPO agentPO) {
        return dslContext.insertInto(AGENT)
                .set(AGENT.NAME, agentPO.getName())
                .set(AGENT.DESCRIPTION, agentPO.getDescription())
                .set(AGENT.STATUS, agentPO.getStatus())
                .returning()
                .fetchOneInto(AgentPO.class);
    }

    /**
     * 根据ID查询Agent
     */
    public AgentPO queryById(DSLContext dslContext, Long id) {
        return dslContext.selectFrom(AGENT)
                .where(AGENT.ID.eq(id))
                .fetchOneInto(AgentPO.class);
    }

    /**
     * 根据名称查询Agent
     */
    public AgentPO queryByName(DSLContext dslContext, String name) {
        return dslContext.selectFrom(AGENT)
                .where(AGENT.NAME.eq(name))
                .fetchOneInto(AgentPO.class);
    }

    /**
     * 查询所有Agent
     */
    public List<AgentPO> queryAll(DSLContext dslContext) {
        return dslContext.selectFrom(AGENT)
                .fetchInto(AgentPO.class);
    }

    /**
     * 根据状态查询Agent列表
     */
    public List<AgentPO> queryByStatus(DSLContext dslContext, String status) {
        return dslContext.selectFrom(AGENT)
                .where(AGENT.STATUS.eq(status))
                .fetchInto(AgentPO.class);
    }

    /**
     * 更新Agent
     */
    public int update(DSLContext dslContext, AgentPO agentPO) {
        return dslContext.update(AGENT)
                .set(AGENT.NAME, agentPO.getName())
                .set(AGENT.DESCRIPTION, agentPO.getDescription())
                .set(AGENT.STATUS, agentPO.getStatus())
                .set(AGENT.UPDATED_AT, agentPO.getUpdatedAt())
                .where(AGENT.ID.eq(agentPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除Agent
     */
    public int deleteById(DSLContext dslContext, Long id) {
        return dslContext.deleteFrom(AGENT)
                .where(AGENT.ID.eq(id))
                .execute();
    }

    /**
     * 批量删除Agent
     */
    public int deleteByIds(DSLContext dslContext, List<Long> ids) {
        return dslContext.deleteFrom(AGENT)
                .where(AGENT.ID.in(ids))
                .execute();
    }
}

