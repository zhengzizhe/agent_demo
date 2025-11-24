package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志持久化对象
 * 对应表：login_log
 */
@Getter
@Setter
public class LoginLogPO {
    /**
     * 雪花ID，主键
     */
    private Long id;

    /**
     * 登录对应的用户ID，可为空（如账号不存在时的失败登录）
     */
    private Long userId;

    /**
     * 登录使用的认证提供方：local_email / google / github / wechat / email_code 等
     */
    private String provider;

    /**
     * 登录类型：password / email_code / oauth / sso 等
     */
    private String loginType;

    /**
     * 是否成功：1成功，0失败
     */
    private Integer success;

    /**
     * 失败原因说明：wrong_password / invalid_code / locked 等
     */
    private String failReason;

    /**
     * 登录IP
     */
    private String ipAddress;

    /**
     * 登录设备UA
     */
    private String userAgent;

    /**
     * 日志创建时间（毫秒时间戳）
     */
    private Long createdAt;
}




