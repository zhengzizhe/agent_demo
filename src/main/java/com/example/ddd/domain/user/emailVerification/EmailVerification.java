package com.example.ddd.domain.user.emailVerification;

import com.example.ddd.domain.user.valueobj.Email;
import com.example.ddd.domain.user.valueobj.VerificationCode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 邮箱验证码聚合根
 * 对应表：email_verification
 */
@Getter
@Setter
@Data
public class EmailVerification {

    /**
     * 雪花ID
     * 对应 email_verification.id
     */
    private Long id;

    /**
     * 验证码对应的邮箱
     * 对应 email_verification.email
     */
    private Email email;

    /**
     * 验证码内容
     * 对应 email_verification.code
     */
    private VerificationCode code;

    /**
     * 验证码用途（注册/登录/找回密码/绑定邮箱）
     * 对应 email_verification.purpose
     */
    private VerificationPurpose purpose;

    /**
     * 验证码状态（未使用、已使用、已失效）
     * 对应 email_verification.status
     */
    private VerificationStatus status;

    /**
     * 过期时间（毫秒时间戳）
     * 对应 email_verification.expire_at
     */
    private long expireAt;

    /**
     * 创建时间（毫秒时间戳）
     * 对应 email_verification.created_at
     */
    private long createdAt;

    /**
     * 实际使用时间（毫秒时间戳）
     * 对应 email_verification.used_at
     */
    private Long usedAt;

    /**
     * 请求验证码的 IP
     * 对应 email_verification.client_ip
     */
    private String clientIp;

    /**
     * 请求验证码时的 User-Agent 信息
     * 对应 email_verification.user_agent
     */
    private String userAgent;

    // === 行为如 markUsed(now) 可自行添加 ====
}
