package com.example.ddd.domain.user.valueobj;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 邮箱验证码值对象
 */
@Getter
@Setter
@Data
public class VerificationCode {

    private final String value;

    public VerificationCode(String value) {
        // 可做长度/数字/字符校验
        this.value = value;
    }

}
