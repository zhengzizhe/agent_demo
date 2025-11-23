package com.example.ddd.infrastructure.dao.mapper;

import com.example.ddd.infrastructure.dao.po.AuthIdentityPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 认证身份 Mapper 接口
 */
@Mapper
public interface AuthIdentityMapper {

    /**
     * 插入认证身份
     */
    int insert(AuthIdentityPO authIdentityPO);

    /**
     * 根据ID查询认证身份
     */
    AuthIdentityPO queryById(@Param("id") Long id);

    /**
     * 根据用户ID查询所有认证身份
     */
    List<AuthIdentityPO> queryByUserId(@Param("userId") Long userId);

    /**
     * 根据提供方和提供方UID查询认证身份
     */
    AuthIdentityPO queryByProviderAndProviderUid(@Param("provider") String provider, @Param("providerUid") String providerUid);

    /**
     * 根据用户ID和提供方查询认证身份
     */
    AuthIdentityPO queryByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);

    /**
     * 更新认证身份
     */
    int update(AuthIdentityPO authIdentityPO);

    /**
     * 根据ID删除认证身份
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID删除所有认证身份
     */
    int deleteByUserId(@Param("userId") Long userId);
}



