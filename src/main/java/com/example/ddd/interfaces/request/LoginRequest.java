package com.example.ddd.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录请求 DTO
 */
@Getter
@Setter
public class LoginRequest {

    /**
     * 登录方式：email / email_code / oauth
     */
    @NotBlank(message = "登录方式不能为空")
    private String loginType;

    /**
     * 邮箱（邮箱密码登录或邮箱验证码登录时使用）
     */
    private String email;

    /**
     * 密码（邮箱密码登录时使用）
     */
    private String password;

    /**
     * 验证码（邮箱验证码登录时使用）
     */
    private String code;

    /**
     * OAuth 提供方（OAuth 登录时使用）：google / github / wechat
     */
    private String provider;

    /**
     * OAuth 授权码（OAuth 登录时使用）
     */
    private String authCode;

    /**
     * OAuth 状态参数（OAuth 登录时使用，用于防止 CSRF）
     */
    private String state;
}




