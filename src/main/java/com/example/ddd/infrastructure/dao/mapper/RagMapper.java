package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.RagPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * RAG Mapper接口
 */
@Mapper
public interface RagMapper {

    /**
     * 插入RAG
     */
    int insert(RagPO ragPO);

    /**
     * 根据ID查询RAG
     */
    RagPO queryById(@Param("id") String id);

    /**
     * 根据名称查询RAG
     */
    RagPO queryByName(@Param("name") String name);

    /**
     * 根据向量存储类型查询RAG列表
     */
    List<RagPO> queryByVectorStoreType(@Param("vectorStoreType") String vectorStoreType);

    /**
     * 查询所有RAG
     */
    List<RagPO> queryAll();

    /**
     * 根据状态查询RAG列表
     */
    List<RagPO> queryByStatus(@Param("status") String status);

    /**
     * 更新RAG
     */
    int update(RagPO ragPO);

    /**
     * 根据ID删除RAG
     */
    int deleteById(@Param("id") String id);

    /**
     * 批量删除RAG
     */
    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 根据Agent ID查询关联的RAG列表
     */
    List<RagPO> queryByAgentId(@Param("agentId") String agentId);

    /**
     * 根据Client ID列表查询RAG列表（通过关联表）
     */
    List<RagPO> queryByClientIds(@Param("clientIds") List<String> clientIds);

    /**
     * 根据Client ID查询RAG列表（通过关联表）
     */
    List<RagPO> queryByClientId(@Param("clientId") String clientId);

    /**
     * 根据Orchestrator ID查询关联的RAG列表
     */
    List<RagPO> queryByOrchestratorId(@Param("orchestratorId") String orchestratorId);
}

