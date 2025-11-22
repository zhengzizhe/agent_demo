package com.example.ddd.domain.user.valueobj;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户ID值对象（雪花ID）
 */
@Getter
@Setter
@Data
public class UserId {

    /** 雪花ID 本身 */
    private final long value;

    public UserId(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
