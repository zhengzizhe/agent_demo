package com.example.ddd.domain.user.enetity;

import com.example.ddd.domain.user.AuthProvider;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户认证方式实体（属于 User 聚合）
 * 对应表：auth_identity
 */
@Getter
@Setter
@Data
public class AuthIdentity {

    /**
     * 身份记录唯一ID（雪花ID）
     * 对应 auth_identity.id
     */
    private Long id;

    /**
     * 登录提供方（本地邮箱、Google、Github、微信等）
     * 对应 auth_identity.provider
     */
    private AuthProvider provider;

    /**
     * 在该 provider 下的用户唯一标识
     * 如：openid、GitHub id、Google sub、邮箱本身等
     * 对应 auth_identity.provider_uid
     */
    private String providerUid;

    /**
     * 扩展信息（JSON 字符串）
     * 存储：头像、昵称、token 等不稳定字段
     * 对应 auth_identity.extra_info(JSONB)
     */
    private String extraInfoJson;

    /**
     * 创建时间（毫秒时间戳）
     * 对应 auth_identity.created_at
     */
    private long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     * 对应 auth_identity.updated_at
     */
    private long updatedAt;

    // ==== 构造方法、行为可根据需要追加 ====
}
