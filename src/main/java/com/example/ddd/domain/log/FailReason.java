package com.example.ddd.domain.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 登录失败原因枚举
 * 对应 login_log.fail_reason 字段
 */
public enum FailReason {
    /**
     * 密码错误
     */
    WRONG_PASSWORD("wrong_password"),

    /**
     * 验证码无效
     */
    INVALID_CODE("invalid_code"),

    /**
     * 验证码已过期
     */
    CODE_EXPIRED("code_expired"),

    /**
     * 账号被锁定
     */
    LOCKED("locked"),

    /**
     * 账号被冻结
     */
    FROZEN("frozen"),

    /**
     * 账号不存在
     */
    USER_NOT_FOUND("user_not_found"),

    /**
     * 账号已注销
     */
    CANCELLED("cancelled"),

    /**
     * OAuth 认证失败
     */
    OAUTH_FAILED("oauth_failed"),

    /**
     * 其他原因
     */
    OTHER("other");

    private final String value;

    FailReason(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static FailReason fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (FailReason reason : FailReason.values()) {
            if (reason.value.equals(value)) {
                return reason;
            }
        }
        return OTHER; // 默认返回 OTHER，避免返回 null
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}




