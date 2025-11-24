package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.DocPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档 Mapper接口
 */
@Mapper
public interface DocMapper {

    /**
     * 插入文档
     */
    int insert(DocPO docPO);

    /**
     * 根据userId查询所有文档
     */
    List<DocPO> queryByUserId(@Param("userId") String userId);

    /**
     * 根据ID查询文档
     */
    DocPO queryById(@Param("id") String id);

    /**
     * 更新文档
     */
    int update(DocPO docPO);

    /**
     * 根据ID删除文档
     */
    int deleteById(@Param("id") String id);
}




