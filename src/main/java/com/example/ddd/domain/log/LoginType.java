package com.example.ddd.domain.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 登录类型枚举
 * 对应 login_log.login_type 字段
 */
public enum LoginType {
    /**
     * 密码登录
     */
    PASSWORD("password"),

    /**
     * 邮箱验证码登录
     */
    EMAIL_CODE("email_code"),

    /**
     * OAuth 登录
     */
    OAUTH("oauth"),

    /**
     * SSO 单点登录
     */
    SSO("sso");

    private final String value;

    LoginType(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static LoginType fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (LoginType type : LoginType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

