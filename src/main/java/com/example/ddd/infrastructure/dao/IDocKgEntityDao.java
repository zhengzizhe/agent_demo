package com.example.ddd.infrastructure.dao;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

/**
 * 文档-知识图谱实体关联数据访问对象
 */
@Singleton
public class IDocKgEntityDao {

    /**
     * 批量插入文档-实体关联
     */
    public void batchInsert(DSLContext dsl, String docId, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return;
        }
        
        // 使用原生SQL插入，因为jOOQ表类可能需要重新生成
        // 不插入created_at，使用表的默认值
        String sql = "INSERT INTO doc_kg_entity (doc_id, kg_entity_id) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (doc_id, kg_entity_id) DO NOTHING";
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            for (Long entityId : entityIds) {
                ps.setString(1, docId);
                ps.setLong(2, entityId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("批量插入文档-实体关联失败", e);
        }
    }

    /**
     * 根据文档ID查询关联的实体ID列表
     */
    public List<Long> queryEntityIdsByDocId(DSLContext dsl, String docId) {
        String sql = "SELECT kg_entity_id FROM doc_kg_entity WHERE doc_id = ?";
        List<Long> result = new java.util.ArrayList<>();
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setString(1, docId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong("kg_entity_id"));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("查询文档关联实体失败", e);
        }
        return result;
    }

    /**
     * 根据实体ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByEntityId(DSLContext dsl, Long entityId) {
        String sql = "SELECT doc_id FROM doc_kg_entity WHERE kg_entity_id = ?";
        List<String> result = new java.util.ArrayList<>();
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, entityId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("doc_id"));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("查询实体关联文档失败", e);
        }
        return result;
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(DSLContext dsl, String docId) {
        String sql = "DELETE FROM doc_kg_entity WHERE doc_id = ?";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setString(1, docId);
            return ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("删除文档关联失败", e);
        }
    }

    /**
     * 删除实体的所有关联
     */
    public int deleteByEntityId(DSLContext dsl, Long entityId) {
        String sql = "DELETE FROM doc_kg_entity WHERE kg_entity_id = ?";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, entityId);
            return ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("删除实体关联失败", e);
        }
    }
}

