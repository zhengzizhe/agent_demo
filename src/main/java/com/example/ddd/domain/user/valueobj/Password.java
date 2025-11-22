package com.example.ddd.domain.user.valueobj;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 密码值对象（永远不存储明文密码）
 */
@Getter
@Setter
@Data
public class Password {

    /** 哈希后的密码 */
    private final String hash;

    /** 可选的盐 */
    private final String salt;

    public Password(String hash, String salt) {
        this.hash = hash;
        this.salt = salt;
    }

    public String getHash() {
        return hash;
    }

    public String getSalt() {
        return salt;
    }
}
