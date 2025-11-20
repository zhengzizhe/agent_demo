package com.example.ddd.infrastructure.dao;

import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

/**
 * RAG-文档关联数据访问对象
 */
@Singleton
public class IRagDocDao {

    /**
     * 插入RAG-文档关联
     */
    public void insert(DSLContext dsl, Long ragId, String docId) {
        String sql = "INSERT INTO rag_doc (rag_id, doc_id) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (rag_id, doc_id) DO NOTHING";
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            ps.setString(2, docId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("插入RAG-文档关联失败", e);
        }
    }

    /**
     * 根据RAG ID查询关联的文档ID列表
     */
    public List<String> queryDocIdsByRagId(DSLContext dsl, Long ragId) {
        String sql = "SELECT doc_id FROM rag_doc WHERE rag_id = ?";
        List<String> result = new java.util.ArrayList<>();
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("doc_id"));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("查询RAG关联文档失败", e);
        }
        return result;
    }

    /**
     * 根据文档ID查询关联的RAG ID列表
     */
    public List<Long> queryRagIdsByDocId(DSLContext dsl, String docId) {
        String sql = "SELECT rag_id FROM rag_doc WHERE doc_id = ?";
        List<Long> result = new java.util.ArrayList<>();
        
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setString(1, docId);
            var rs = ps.executeQuery();
            while (rs.next()) {
                result.add(rs.getLong("rag_id"));
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("查询文档关联RAG失败", e);
        }
        return result;
    }

    /**
     * 删除RAG的所有关联
     */
    public int deleteByRagId(DSLContext dsl, Long ragId) {
        String sql = "DELETE FROM rag_doc WHERE rag_id = ?";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            return ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("删除RAG关联失败", e);
        }
    }

    /**
     * 删除文档的所有关联
     */
    public int deleteByDocId(DSLContext dsl, String docId) {
        String sql = "DELETE FROM rag_doc WHERE doc_id = ?";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setString(1, docId);
            return ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("删除文档关联失败", e);
        }
    }

    /**
     * 删除特定的RAG-文档关联
     */
    public int delete(DSLContext dsl, Long ragId, String docId) {
        String sql = "DELETE FROM rag_doc WHERE rag_id = ? AND doc_id = ?";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            var ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            ps.setString(2, docId);
            return ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("删除RAG-文档关联失败", e);
        }
    }
}

