package com.example.ddd.domain.user.valueobj;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 邮箱值对象（负责校验）
 */
@Getter
@Setter
@Data
public class Email {

    private final String value;

    public Email(String value) {
        // 这里可做正则校验
        this.value = value.toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
