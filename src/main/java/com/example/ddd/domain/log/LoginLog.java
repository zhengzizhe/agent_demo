package com.example.ddd.domain.log;

import com.example.ddd.domain.user.AuthProvider;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志实体（审计日志）
 * 对应表：login_log
 */
@Getter
@Setter
@Data
public class LoginLog {

    /**
     * 日志ID（雪花ID）
     * 对应 login_log.id
     */
    private Long id;

    /**
     * 登录的用户ID，可能为空
     * （例如账号不存在时的登录失败）
     * 对应 login_log.user_id
     */
    private Long userId;

    /**
     * 登录使用的认证提供方
     * 对应 login_log.provider
     */
    private AuthProvider provider;

    /**
     * 登录类型（密码、验证码、OAuth、SSO）
     * 对应 login_log.login_type
     */
    private LoginType loginType;

    /**
     * 登录是否成功（1成功 0失败）
     * 对应 login_log.success
     */
    private boolean success;

    /**
     * 登录失败原因（如密码错误、验证码错误）
     * 对应 login_log.fail_reason
     */
    private FailReason failReason;

    /**
     * 用户登录IP
     * 对应 login_log.ip_address
     */
    private String ipAddress;

    /**
     * 用户设备 UA
     * 对应 login_log.user_agent
     */
    private String userAgent;

    /**
     * 日志创建时间（毫秒时间戳）
     * 对应 login_log.created_at
     */
    private long createdAt;
}
