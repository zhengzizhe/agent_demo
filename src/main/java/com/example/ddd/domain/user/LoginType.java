package com.example.ddd.domain.user;

import com.example.ddd.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.ddd.common.exception.ErrorCode.INTERNAL_ERROR;

@Getter

@AllArgsConstructor
public enum LoginType {
    Email(1, "email");
    private Integer code;
    private String desc;


    public static LoginType by(Integer code) {
        for (LoginType value : LoginType.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new BusinessException(INTERNAL_ERROR);
    }
}
