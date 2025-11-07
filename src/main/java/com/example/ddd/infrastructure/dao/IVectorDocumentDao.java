package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.VectorDocumentPO;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;
import org.jooq.JSONB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 向量文档数据访问对象
 * 使用原生SQL处理pgvector类型
 */
@Singleton
public class IVectorDocumentDao {

    /**
     * 插入向量文档
     */
    public VectorDocumentPO insert(DSLContext dslContext, VectorDocumentPO vectorDocumentPO) {
        String sql = "INSERT INTO vector_document (rag_id, content, embedding, metadata, chunk_index) " +
                "VALUES (?, ?, ?::vector, ?::jsonb, ?) RETURNING *";
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, vectorDocumentPO.getRagId());
            ps.setString(2, vectorDocumentPO.getContent());
            
            // 将float[]转换为pgvector格式的字符串: "[0.1,0.2,0.3]"
            if (vectorDocumentPO.getEmbedding() != null) {
                ps.setString(3, arrayToString(vectorDocumentPO.getEmbedding()));
            } else {
                ps.setString(3, null);
            }
            
            // 设置metadata
            if (vectorDocumentPO.getMetadata() != null) {
                // JSONB对象已经有data()方法可以直接获取JSON字符串
                ps.setString(4, vectorDocumentPO.getMetadata().data());
            } else {
                ps.setString(4, null);
            }
            
            ps.setInt(5, vectorDocumentPO.getChunkIndex() != null ? vectorDocumentPO.getChunkIndex() : 0);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToPO(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert vector document", e);
        }
        return null;
    }

    /**
     * 批量插入向量文档
     */
    public int batchInsert(DSLContext dslContext, List<VectorDocumentPO> documents) {
        String sql = "INSERT INTO vector_document (rag_id, content, embedding, metadata, chunk_index) " +
                "VALUES (?, ?, ?::vector, ?::jsonb, ?)";
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            
            for (VectorDocumentPO doc : documents) {
                ps.setLong(1, doc.getRagId());
                ps.setString(2, doc.getContent());
                
                if (doc.getEmbedding() != null) {
                    ps.setString(3, arrayToString(doc.getEmbedding()));
                } else {
                    ps.setString(3, null);
                }
                
                if (doc.getMetadata() != null) {
                    // JSONB对象已经有data()方法可以直接获取JSON字符串
                    ps.setString(4, doc.getMetadata().data());
                } else {
                    ps.setString(4, null);
                }
                
                ps.setInt(5, doc.getChunkIndex() != null ? doc.getChunkIndex() : 0);
                ps.addBatch();
            }
            
            return ps.executeBatch().length;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to batch insert vector documents", e);
        }
    }

    /**
     * 根据ID查询向量文档
     */
    public VectorDocumentPO queryById(DSLContext dslContext, Long id) {
        String sql = "SELECT * FROM vector_document WHERE id = ?";
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToPO(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query vector document by id", e);
        }
        return null;
    }

    /**
     * 根据RAG ID查询所有向量文档
     */
    public List<VectorDocumentPO> queryByRagId(DSLContext dslContext, Long ragId) {
        String sql = "SELECT * FROM vector_document WHERE rag_id = ? ORDER BY chunk_index";
        List<VectorDocumentPO> result = new ArrayList<>();
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(mapResultSetToPO(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query vector documents by rag_id", e);
        }
        return result;
    }

    /**
     * 向量相似度搜索（使用余弦相似度）
     * @param ragId RAG ID
     * @param queryEmbedding 查询向量
     * @param limit 返回结果数量限制
     * @param similarityThreshold 相似度阈值（0-1之间，值越大要求越相似）
     * @return 相似文档列表，按相似度降序排列
     */
    public List<VectorDocumentPO> similaritySearch(DSLContext dslContext, Long ragId, 
                                                    float[] queryEmbedding, int limit, 
                                                    double similarityThreshold) {
        String sql = "SELECT *, 1 - (embedding <=> ?::vector) as similarity " +
                "FROM vector_document " +
                "WHERE rag_id = ? AND (1 - (embedding <=> ?::vector)) >= ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?";
        
        List<VectorDocumentPO> result = new ArrayList<>();
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            String embeddingStr = arrayToString(queryEmbedding);
            
            ps.setString(1, embeddingStr);
            ps.setLong(2, ragId);
            ps.setString(3, embeddingStr);
            ps.setDouble(4, similarityThreshold);
            ps.setString(5, embeddingStr);
            ps.setInt(6, limit);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                VectorDocumentPO po = mapResultSetToPO(rs);
                result.add(po);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to perform similarity search", e);
        }
        return result;
    }

    /**
     * 根据RAG ID删除所有向量文档
     */
    public int deleteByRagId(DSLContext dslContext, Long ragId) {
        String sql = "DELETE FROM vector_document WHERE rag_id = ?";
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, ragId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete vector documents by rag_id", e);
        }
    }

    /**
     * 根据ID删除向量文档
     */
    public int deleteById(DSLContext dslContext, Long id) {
        String sql = "DELETE FROM vector_document WHERE id = ?";
        
        try (Connection connection = dslContext.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete vector document by id", e);
        }
    }

    /**
     * 将float[]转换为pgvector格式字符串
     */
    private String arrayToString(float[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将pgvector格式字符串转换为float[]
     */
    private float[] stringToArray(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        // 移除方括号并分割
        str = str.trim();
        if (str.startsWith("[") && str.endsWith("]")) {
            str = str.substring(1, str.length() - 1);
        }
        String[] parts = str.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }

    /**
     * 将ResultSet映射为VectorDocumentPO
     */
    private VectorDocumentPO mapResultSetToPO(ResultSet rs) throws SQLException {
        VectorDocumentPO po = new VectorDocumentPO();
        po.setId(rs.getLong("id"));
        po.setRagId(rs.getLong("rag_id"));
        po.setContent(rs.getString("content"));
        
        // 处理embedding向量
        String embeddingStr = rs.getString("embedding");
        if (embeddingStr != null) {
            po.setEmbedding(stringToArray(embeddingStr));
        }
        
        // 处理metadata JSONB
        String metadataStr = rs.getString("metadata");
        if (metadataStr != null && !metadataStr.isEmpty()) {
            po.setMetadata(JSONB.valueOf(metadataStr));
        }
        
        po.setChunkIndex(rs.getInt("chunk_index"));
        po.setCreatedAt(rs.getLong("created_at"));
        po.setUpdatedAt(rs.getLong("updated_at"));
        return po;
    }
}

