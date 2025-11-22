package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.UserAccountPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户账户 Mapper 接口
 */
@Mapper
public interface UserAccountMapper {

    /**
     * 插入用户账户
     */
    int insert(UserAccountPO userAccountPO);

    /**
     * 根据ID查询用户账户
     */
    UserAccountPO queryById(@Param("id") Long id);

    /**
     * 根据UUID查询用户账户
     */
    UserAccountPO queryByUuid(@Param("uuid") String uuid);

    /**
     * 根据邮箱查询用户账户
     */
    UserAccountPO queryByEmail(@Param("email") String email);

    /**
     * 更新用户账户
     */
    int update(UserAccountPO userAccountPO);

    /**
     * 根据ID删除用户账户（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新最后登录时间
     */
    int updateLastLoginAt(@Param("id") Long id, @Param("lastLoginAt") Long lastLoginAt);

    /**
     * 根据邮箱和状态查询用户账户列表
     */
    List<UserAccountPO> queryByEmailAndStatus(@Param("email") String email, @Param("status") Integer status);

    /**
     * 根据状态查询用户账户列表
     */
    List<UserAccountPO> queryByStatus(@Param("status") Integer status);
}

