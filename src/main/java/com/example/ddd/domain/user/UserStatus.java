package com.example.ddd.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 用户状态枚举
 * 对应 user_account.status 字段
 */
public enum UserStatus {
    /**
     * 正常状态
     */
    NORMAL(1, "normal"),

    /**
     * 冻结状态
     */
    FROZEN(0, "frozen"),

    /**
     * 注销状态
     */
    CANCELLED(2, "cancelled");

    private final int code;
    private final String value;

    UserStatus(int code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 根据数据库代码获取枚举
     */
    public static UserStatus fromCode(int code) {
        for (UserStatus status : UserStatus.values()) {
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
    public static UserStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (UserStatus status : UserStatus.values()) {
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




