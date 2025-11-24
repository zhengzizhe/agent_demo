package com.example.ddd.domain.user.emailVerification;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 验证码状态枚举
 * 对应 email_verification.status 字段
 */
public enum VerificationStatus {
    /**
     * 未使用
     */
    UNUSED(0, "unused"),

    /**
     * 已使用
     */
    USED(1, "used"),

    /**
     * 已失效
     */
    EXPIRED(2, "expired");

    private final int code;
    private final String value;

    VerificationStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 根据数据库代码获取枚举
     */
    public static VerificationStatus fromCode(int code) {
        for (VerificationStatus status : VerificationStatus.values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 根据字符串值获取枚举
     */
    @JsonCreator
    public static VerificationStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (VerificationStatus status : VerificationStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 获取数据库代码
     */
    public int getCode() {
        return code;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}




