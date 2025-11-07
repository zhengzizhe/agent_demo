package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.McpPO;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

import static com.example.jooq.tables.Mcp.MCP;

/**
 * MCP数据访问对象
 */
@Singleton
public class IMcpDao {

    /**
     * 插入MCP
     */
    public McpPO insert(DSLContext dslContext, McpPO mcpPO) {
        return dslContext.insertInto(MCP)
                .set(MCP.NAME, mcpPO.getName())
                .set(MCP.TYPE, mcpPO.getType())
                .set(MCP.ENDPOINT, mcpPO.getEndpoint())
                .set(MCP.CONFIG, mcpPO.getConfig())
                .set(MCP.STATUS, mcpPO.getStatus())
                .returning()
                .fetchOneInto(McpPO.class);
    }

    /**
     * 根据ID查询MCP
     */
    public McpPO queryById(DSLContext dslContext, Long id) {
        return dslContext.selectFrom(MCP)
                .where(MCP.ID.eq(id))
                .fetchOneInto(McpPO.class);
    }

    /**
     * 根据名称查询MCP
     */
    public McpPO queryByName(DSLContext dslContext, String name) {
        return dslContext.selectFrom(MCP)
                .where(MCP.NAME.eq(name))
                .fetchOneInto(McpPO.class);
    }

    /**
     * 根据类型查询MCP列表
     */
    public List<McpPO> queryByType(DSLContext dslContext, String type) {
        return dslContext.selectFrom(MCP)
                .where(MCP.TYPE.eq(type))
                .fetchInto(McpPO.class);
    }

    /**
     * 查询所有MCP
     */
    public List<McpPO> queryAll(DSLContext dslContext) {
        return dslContext.selectFrom(MCP)
                .fetchInto(McpPO.class);
    }

    /**
     * 根据状态查询MCP列表
     */
    public List<McpPO> queryByStatus(DSLContext dslContext, String status) {
        return dslContext.selectFrom(MCP)
                .where(MCP.STATUS.eq(status))
                .fetchInto(McpPO.class);
    }

    /**
     * 更新MCP
     */
    public int update(DSLContext dslContext, McpPO mcpPO) {
        return dslContext.update(MCP)
                .set(MCP.NAME, mcpPO.getName())
                .set(MCP.TYPE, mcpPO.getType())
                .set(MCP.ENDPOINT, mcpPO.getEndpoint())
                .set(MCP.CONFIG, mcpPO.getConfig())
                .set(MCP.STATUS, mcpPO.getStatus())
                .set(MCP.UPDATED_AT, mcpPO.getUpdatedAt())
                .where(MCP.ID.eq(mcpPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除MCP
     */
    public int deleteById(DSLContext dslContext, Long id) {
        return dslContext.deleteFrom(MCP)
                .where(MCP.ID.eq(id))
                .execute();
    }

    /**
     * 批量删除MCP
     */
    public int deleteByIds(DSLContext dslContext, List<Long> ids) {
        return dslContext.deleteFrom(MCP)
                .where(MCP.ID.in(ids))
                .execute();
    }
}

