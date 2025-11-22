package com.example.ddd.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token 类型（通常是 "Bearer"）
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token 过期时间（毫秒时间戳）
     */
    private Long expiresAt;
    /**
     * 用户UUID（对外标识）
     */
    private String userUuid;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户显示名称
     */
    private String displayName;

    /**
     * 是否首次登录
     */
    private Boolean isFirstLogin;
}

