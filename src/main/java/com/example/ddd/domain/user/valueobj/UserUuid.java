package com.example.ddd.domain.user.valueobj;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 对外用户唯一标识（UUID/短UUID）
 */
@Getter
@Setter
@Data
public class UserUuid {

    private final String value;

    public UserUuid(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
