package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.EmailVerificationPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 邮箱验证码 Mapper 接口
 */
@Mapper
public interface EmailVerificationMapper {

    /**
     * 插入邮箱验证码
     */
    int insert(EmailVerificationPO emailVerificationPO);

    /**
     * 根据ID查询邮箱验证码
     */
    EmailVerificationPO queryById(@Param("id") Long id);

    /**
     * 根据邮箱、验证码和用途查询最新的验证码记录
     */
    EmailVerificationPO queryLatestByEmailAndCodeAndPurpose(
            @Param("email") String email,
            @Param("code") String code,
            @Param("purpose") String purpose);

    /**
     * 根据邮箱和用途查询最新的验证码记录
     */
    EmailVerificationPO queryLatestByEmailAndPurpose(
            @Param("email") String email,
            @Param("purpose") String purpose);

    /**
     * 根据邮箱和用途查询验证码列表（按创建时间倒序）
     */
    List<EmailVerificationPO> queryByEmailAndPurpose(
            @Param("email") String email,
            @Param("purpose") String purpose);

    /**
     * 更新验证码状态
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("status") Integer status,
            @Param("usedAt") Long usedAt);

    /**
     * 标记验证码为已使用
     */
    int markAsUsed(@Param("id") Long id, @Param("usedAt") Long usedAt);

    /**
     * 标记验证码为已失效
     */
    int markAsExpired(@Param("id") Long id);

    /**
     * 删除过期的验证码记录
     */
    int deleteExpired(@Param("currentTime") Long currentTime);
}

