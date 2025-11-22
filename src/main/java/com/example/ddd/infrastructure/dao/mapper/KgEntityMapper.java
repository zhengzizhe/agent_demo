package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识图谱实体 Mapper接口
 */
@Mapper
public interface KgEntityMapper {

    /**
     * 插入知识图谱实体
     */
    int insert(KgEntityPO kgEntityPO);

    /**
     * 根据ID查询知识图谱实体
     */
    KgEntityPO queryById(@Param("id") String id);

    /**
     * 查询所有知识图谱实体
     */
    List<KgEntityPO> queryAll();

    /**
     * 根据类型查询知识图谱实体
     */
    List<KgEntityPO> queryByType(@Param("type") String type);

    /**
     * 根据名称模糊查询知识图谱实体
     */
    List<KgEntityPO> queryByNameLike(@Param("name") String name);

    /**
     * 更新知识图谱实体
     */
    int update(KgEntityPO kgEntityPO);

    /**
     * 根据ID删除知识图谱实体
     */
    int deleteById(@Param("id") String id);

    /**
     * 批量插入知识图谱实体
     */
    void batchInsert(@Param("list") List<KgEntityPO> list);

    /**
     * 向量相似度搜索
     */
    List<KgEntityPO> similaritySearch(@Param("queryEmbedding") String queryEmbedding,
                                      @Param("limit") int limit,
                                      @Param("similarityThreshold") double similarityThreshold);
}

