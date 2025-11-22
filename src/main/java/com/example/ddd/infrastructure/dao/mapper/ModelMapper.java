package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.ModelPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Model Mapper接口
 */
@Mapper
public interface ModelMapper {

    /**
     * 插入Model
     */
    int insert(ModelPO modelPO);

    /**
     * 根据ID查询Model
     */
    ModelPO queryById(@Param("id") String id);

    /**
     * 根据名称查询Model
     */
    ModelPO queryByName(@Param("name") String name);

    /**
     * 根据提供商查询Model列表
     */
    List<ModelPO> queryByProvider(@Param("provider") String provider);

    /**
     * 查询所有Model
     */
    List<ModelPO> queryAll();

    /**
     * 根据状态查询Model列表
     */
    List<ModelPO> queryByStatus(@Param("status") String status);

    /**
     * 更新Model
     */
    int update(ModelPO modelPO);

    /**
     * 根据ID删除Model
     */
    int deleteById(@Param("id") String id);

    /**
     * 批量删除Model
     */
    int deleteByIds(@Param("ids") List<String> ids);

    /**
     * 根据Client ID列表查询Model列表（通过关联表）
     */
    List<ModelPO> queryByClientIds(@Param("clientIds") List<String> clientIds);

    /**
     * 根据Client ID查询Model列表（通过关联表）
     */
    List<ModelPO> queryByClientId(@Param("clientId") String clientId);

    /**
     * 根据Agent ID查询关联的Model列表
     */
    List<ModelPO> queryByAgentId(@Param("agentId") String agentId);

    /**
     * 根据Orchestrator ID查询关联的Model列表
     */
    List<ModelPO> queryByOrchestratorId(@Param("orchestratorId") String orchestratorId);
}

