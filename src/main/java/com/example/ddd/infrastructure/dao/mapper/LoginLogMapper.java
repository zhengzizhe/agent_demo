package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.LoginLogPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 登录日志 Mapper 接口
 */
@Mapper
public interface LoginLogMapper {

    /**
     * 插入登录日志
     */
    int insert(LoginLogPO loginLogPO);

    /**
     * 根据ID查询登录日志
     */
    LoginLogPO queryById(@Param("id") Long id);

    /**
     * 根据用户ID查询登录日志列表（按时间倒序）
     */
    List<LoginLogPO> queryByUserId(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 根据用户ID和成功状态查询登录日志列表
     */
    List<LoginLogPO> queryByUserIdAndSuccess(
            @Param("userId") Long userId,
            @Param("success") Integer success,
            @Param("limit") Integer limit);

    /**
     * 根据IP地址查询登录日志列表
     */
    List<LoginLogPO> queryByIpAddress(@Param("ipAddress") String ipAddress, @Param("limit") Integer limit);

    /**
     * 根据时间范围查询登录日志列表
     */
    List<LoginLogPO> queryByTimeRange(
            @Param("startTime") Long startTime,
            @Param("endTime") Long endTime,
            @Param("limit") Integer limit);

    /**
     * 统计用户登录次数
     */
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计用户成功登录次数
     */
    Long countSuccessByUserId(@Param("userId") Long userId);

    /**
     * 统计用户失败登录次数
     */
    Long countFailByUserId(@Param("userId") Long userId);
}

