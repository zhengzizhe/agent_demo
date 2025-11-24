package com.example.ddd.common.exception;

import lombok.Getter;

/**
 * 业务异常
 * 用于抛出业务逻辑相关的异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    /**
     * 错误码
     */
    private final ErrorCode errorCode;
    
    /**
     * 错误信息（可自定义，覆盖枚举中的默认信息）
     */
    private final String customMessage;
    
    /**
     * 构造方法（使用错误码枚举）
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }
    
    /**
     * 构造方法（使用错误码枚举 + 自定义消息）
     */
    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage != null ? customMessage : errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    /**
     * 构造方法（使用错误码枚举 + 自定义消息 + 原因）
     */
    public BusinessException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage != null ? customMessage : errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    /**
     * 获取错误信息（优先使用自定义消息）
     */
    public String getErrorMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}




