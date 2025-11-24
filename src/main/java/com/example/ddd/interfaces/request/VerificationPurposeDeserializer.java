package com.example.ddd.interfaces.request;

import com.example.ddd.domain.user.emailVerification.VerificationPurpose;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * VerificationPurpose 反序列化器
 * 支持从字符串转换为枚举
 */
public class VerificationPurposeDeserializer extends JsonDeserializer<VerificationPurpose> {
    
    @Override
    public VerificationPurpose deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        VerificationPurpose purpose = VerificationPurpose.fromValue(value.trim());
        if (purpose == null) {
            throw new IllegalArgumentException("无效的验证码用途: " + value + 
                    "，支持的值: register, login, reset_password, bind_email");
        }
        return purpose;
    }
}




