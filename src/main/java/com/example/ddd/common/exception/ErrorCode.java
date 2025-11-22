package com.example.ddd.common.exception;

import lombok.Getter;

/**
 * 错误码枚举
 * 定义系统所有错误码和错误信息
 */
@Getter
public enum ErrorCode {
    
    // ========== 通用错误码 (1000-1999) ==========
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 系统内部错误
     */
    INTERNAL_ERROR(500, "系统内部错误"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 参数为空
     */
    PARAM_NULL(400, "参数不能为空"),
    
    /**
     * 参数格式错误
     */
    PARAM_FORMAT_ERROR(400, "参数格式错误"),
    
    /**
     * 未找到资源
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),
    
    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),
    
    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    
    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    
    // ========== 认证相关错误码 (2000-2999) ==========
    /**
     * 登录失败
     */
    LOGIN_FAILED(2001, "登录失败"),
    
    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS(2002, "用户名或密码错误"),
    
    /**
     * 验证码错误
     */
    INVALID_CODE(2003, "验证码错误"),
    
    /**
     * 验证码已过期
     */
    CODE_EXPIRED(2004, "验证码已过期"),
    
    /**
     * 验证码已使用
     */
    CODE_USED(2005, "验证码已使用"),
    
    /**
     * Token 无效
     */
    INVALID_TOKEN(2006, "Token 无效"),
    
    /**
     * Token 已过期
     */
    TOKEN_EXPIRED(2007, "Token 已过期"),
    
    /**
     * Token 缺失
     */
    TOKEN_MISSING(2008, "Token 缺失"),
    
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(2009, "用户不存在"),
    
    /**
     * 用户已被禁用
     */
    USER_DISABLED(2010, "用户已被禁用"),
    
    /**
     * 用户已被冻结
     */
    USER_FROZEN(2011, "用户已被冻结"),
    
    /**
     * 用户已注销
     */
    USER_CANCELED(2012, "用户已注销"),
    
    /**
     * 邮箱已被注册
     */
    EMAIL_ALREADY_EXISTS(2013, "邮箱已被注册"),
    
    /**
     * 邮箱未注册
     */
    EMAIL_NOT_REGISTERED(2014, "邮箱未注册"),
    
    /**
     * 密码错误
     */
    PASSWORD_ERROR(2015, "密码错误"),
    
    /**
     * 密码强度不足
     */
    PASSWORD_WEAK(2016, "密码强度不足"),
    
    // ========== 业务相关错误码 (3000-3999) ==========
    /**
     * 业务逻辑错误
     */
    BUSINESS_ERROR(3000, "业务逻辑错误"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(3001, "操作失败"),
    
    /**
     * 数据已存在
     */
    DATA_ALREADY_EXISTS(3002, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_FOUND(3003, "数据不存在"),
    
    /**
     * 数据状态错误
     */
    DATA_STATE_ERROR(3004, "数据状态错误"),
    
    /**
     * 操作不允许
     */
    OPERATION_NOT_ALLOWED(3005, "操作不允许"),
    
    // ========== Agent 相关错误码 (4000-4999) ==========
    /**
     * Orchestrator 不存在
     */
    ORCHESTRATOR_NOT_FOUND(4001, "Orchestrator 不存在"),
    
    /**
     * Agent 不存在
     */
    AGENT_NOT_FOUND(4002, "Agent 不存在"),
    
    /**
     * RAG 不存在
     */
    RAG_NOT_FOUND(4003, "RAG 不存在"),
    
    /**
     * 任务执行失败
     */
    TASK_EXECUTION_FAILED(4004, "任务执行失败"),
    
    /**
     * 任务计划生成失败
     */
    TASK_PLAN_FAILED(4005, "任务计划生成失败"),
    
    /**
     * 图构建失败
     */
    GRAPH_BUILD_FAILED(4006, "图构建失败"),
    
    // ========== RAG 相关错误码 (5000-5999) ==========
    /**
     * 文档不存在
     */
    DOC_NOT_FOUND(5001, "文档不存在"),
    
    /**
     * 文档上传失败
     */
    DOC_UPLOAD_FAILED(5002, "文档上传失败"),
    
    /**
     * 文档解析失败
     */
    DOC_PARSE_FAILED(5003, "文档解析失败"),
    
    /**
     * 向量化失败
     */
    VECTORIZATION_FAILED(5004, "向量化失败"),
    
    /**
     * 检索失败
     */
    SEARCH_FAILED(5005, "检索失败"),
    
    // ========== 外部服务错误码 (6000-6999) ==========
    /**
     * 外部服务调用失败
     */
    EXTERNAL_SERVICE_ERROR(6000, "外部服务调用失败"),
    
    /**
     * OpenAI API 调用失败
     */
    OPENAI_API_ERROR(6001, "OpenAI API 调用失败"),
    
    /**
     * 数据库操作失败
     */
    DATABASE_ERROR(6002, "数据库操作失败"),
    
    /**
     * 网络请求失败
     */
    NETWORK_ERROR(6003, "网络请求失败"),
    
    /**
     * 文件操作失败
     */
    FILE_OPERATION_ERROR(6004, "文件操作失败");
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误信息
     */
    private final String message;
    
    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    /**
     * 根据错误码获取枚举
     */
    public static ErrorCode fromCode(Integer code) {
        for (ErrorCode errorCode : ErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return INTERNAL_ERROR;
    }
}

