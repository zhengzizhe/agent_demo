package com.example.ddd.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 认证提供方枚举
 * 对应 auth_identity.provider 和 login_log.provider 字段
 */
public enum AuthProvider {
    /**
     * 本地邮箱密码登录
     */
    LOCAL_EMAIL("local_email"),

    /**
     * 邮箱验证码登录
     */
    EMAIL_CODE("email_code"),

    /**
     * Google OAuth
     */
    GOOGLE("google"),

    /**
     * GitHub OAuth
     */
    GITHUB("github"),

    /**
     * 微信登录
     */
    WECHAT("wechat"),

    /**
     * 企业 SSO
     */
    SSO_COMPANY("sso_company");

    private final String value;

    AuthProvider(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static AuthProvider fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (AuthProvider provider : AuthProvider.values()) {
            if (provider.value.equals(value)) {
                return provider;
            }
        }
        return null;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}

