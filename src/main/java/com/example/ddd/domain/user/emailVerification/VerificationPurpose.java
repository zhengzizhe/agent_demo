package com.example.ddd.domain.user.emailVerification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 验证码用途枚举
 * 对应 email_verification.purpose 字段
 */
public enum VerificationPurpose {
    /**
     * 注册
     */
    REGISTER("register"),

    /**
     * 登录
     */
    LOGIN("login"),

    /**
     * 找回密码
     */
    RESET_PASSWORD("reset_password"),

    /**
     * 绑定邮箱
     */
    BIND_EMAIL("bind_email");

    private final String value;

    VerificationPurpose(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static VerificationPurpose fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (VerificationPurpose purpose : VerificationPurpose.values()) {
            if (purpose.value.equals(value)) {
                return purpose;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}




