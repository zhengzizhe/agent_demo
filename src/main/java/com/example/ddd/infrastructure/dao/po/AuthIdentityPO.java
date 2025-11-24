package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 认证身份持久化对象
 * 对应表：auth_identity
 */
@Getter
@Setter
public class AuthIdentityPO {
    /**
     * 雪花ID，主键
     */
    private Long id;

    /**
     * 关联 user_account.id
     */
    private Long userId;

    /**
     * 认证提供方：local_email / google / github / wechat / sso_company 等
     */
    private String provider;

    /**
     * 在该 provider 下的唯一标识（如 openid/sub/email）
     */
    private String providerUid;

    /**
     * 扩展信息，JSONB 存储
     */
    private String extraInfo;

    /**
     * 创建时间（毫秒时间戳，UTC）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳，UTC）
     */
    private Long updatedAt;
}




