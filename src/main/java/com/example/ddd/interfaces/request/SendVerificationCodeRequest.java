package com.example.ddd.interfaces.request;

import com.example.ddd.domain.user.emailVerification.VerificationPurpose;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 发送验证码请求 DTO
 */
@Getter
@Setter
public class SendVerificationCodeRequest {

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 验证码用途
     */
    @NotNull(message = "验证码用途不能为空")
    @JsonDeserialize(using = VerificationPurposeDeserializer.class)
    private VerificationPurpose purpose;
}

