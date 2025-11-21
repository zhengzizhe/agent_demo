package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static com.example.jooq.tables.KgEntity.KG_ENTITY;

/**
 * 知识图谱实体数据访问对象
 */
@Singleton
public class IKgEntityDao {

    /**
     * 插入知识图谱实体
     */
    public KgEntityPO insert(DSLContext dslContext, KgEntityPO kgEntityPO) {
        return dslContext.insertInto(KG_ENTITY)
                .set(KG_ENTITY.ID, kgEntityPO.getId())
                .set(KG_ENTITY.NAME, kgEntityPO.getName())
                .set(KG_ENTITY.TYPE, kgEntityPO.getType())
                .set(KG_ENTITY.DESCRIPTION, kgEntityPO.getDescription())
                .set(KG_ENTITY.EMBEDDING, kgEntityPO.getEmbedding())
                .returning()
                .fetchOneInto(KgEntityPO.class);
    }

    /**
     * 根据ID查询知识图谱实体
     */
    public KgEntityPO queryById(DSLContext dslContext, Long id) {
        return dslContext.selectFrom(KG_ENTITY)
                .where(KG_ENTITY.ID.eq(id))
                .fetchOneInto(KgEntityPO.class);
    }

    /**
     * 查询所有知识图谱实体
     */
    public List<KgEntityPO> queryAll(DSLContext dslContext) {
        return dslContext.selectFrom(KG_ENTITY)
                .fetchInto(KgEntityPO.class);
    }

    /**
     * 根据类型查询知识图谱实体
     */
    public List<KgEntityPO> queryByType(DSLContext dslContext, String type) {
        return dslContext.selectFrom(KG_ENTITY)
                .where(KG_ENTITY.TYPE.eq(type))
                .fetchInto(KgEntityPO.class);
    }

    /**
     * 根据名称模糊查询知识图谱实体
     */
    public List<KgEntityPO> queryByNameLike(DSLContext dslContext, String name) {
        return dslContext.selectFrom(KG_ENTITY)
                .where(KG_ENTITY.NAME.like("%" + name + "%"))
                .fetchInto(KgEntityPO.class);
    }

    /**
     * 更新知识图谱实体
     */
    public int update(DSLContext dslContext, KgEntityPO kgEntityPO) {
        return dslContext.update(KG_ENTITY)
                .set(KG_ENTITY.NAME, kgEntityPO.getName())
                .set(KG_ENTITY.TYPE, kgEntityPO.getType())
                .set(KG_ENTITY.DESCRIPTION, kgEntityPO.getDescription())
                .set(KG_ENTITY.EMBEDDING, kgEntityPO.getEmbedding())
                .where(KG_ENTITY.ID.eq(kgEntityPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除知识图谱实体
     */
    public int deleteById(DSLContext dslContext, Long id) {
        return dslContext.deleteFrom(KG_ENTITY)
                .where(KG_ENTITY.ID.eq(id))
                .execute();
    }

    /**
     * 向量相似度搜索（使用余弦相似度）
     * @param queryEmbedding 查询向量
     * @param limit 返回结果数量限制
     * @param similarityThreshold 相似度阈值（0-1之间，值越大要求越相似）
     * @return 相似实体列表，按相似度降序排列
     */
    public List<KgEntityPO> similaritySearch(DSLContext dslContext, float[] queryEmbedding, int limit, double similarityThreshold) {
        String sql = "SELECT *, 1 - (embedding <=> ?::vector) as similarity " +
                "FROM kg_entity " +
                "WHERE (1 - (embedding <=> ?::vector)) >= ? " +
                "ORDER BY embedding <=> ?::vector " +
                "LIMIT ?";
        
        List<KgEntityPO> result = new java.util.ArrayList<>();
        
        try (var connection = dslContext.configuration().connectionProvider().acquire()) {
            java.sql.PreparedStatement ps = connection.prepareStatement(sql);
            String embeddingStr = arrayToString(queryEmbedding);
            
            ps.setString(1, embeddingStr);
            ps.setString(2, embeddingStr);
            ps.setDouble(3, similarityThreshold);
            ps.setString(4, embeddingStr);
            ps.setInt(5, limit);
            
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                KgEntityPO po = new KgEntityPO();
                po.setId(rs.getLong("id"));
                po.setName(rs.getString("name"));
                po.setType(rs.getString("type"));
                po.setDescription(rs.getString("description"));
                // embedding字段暂时不设置，因为查询结果中不需要
                result.add(po);
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("执行知识图谱相似度搜索失败", e);
        }
        return result;
    }

    /**
     * 批量插入知识图谱实体
     * 使用原生SQL处理pgvector类型，因为jOOQ无法直接处理float[]
     */
    public void batchInsert(DSLContext dsl, List<KgEntityPO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO kg_entity (id, name, type, description, embedding) " +
                "VALUES (?, ?, ?, ?, ?::vector)";
        try (var connection = dsl.configuration().connectionProvider().acquire()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (KgEntityPO po : list) {
                ps.setLong(1, po.getId());
                ps.setString(2, po.getName());
                ps.setString(3, po.getType());
                ps.setString(4, po.getDescription());
                Object embeddingObj = po.getEmbedding();
                if (embeddingObj != null && embeddingObj instanceof float[]) {
                    ps.setString(5, arrayToString((float[]) embeddingObj));
                } else {
                    ps.setString(5, null);
                }
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("批量插入知识图谱实体失败", e);
        }
    }
    
    /**
     * 将float[]转换为pgvector格式的字符串: "[0.1,0.2,0.3]"
     */
    private String arrayToString(float[] array) {
        if (array == null || array.length == 0) {
            return "[]";
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
}

