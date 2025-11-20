package com.example.ddd.trigger.request;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * 添加文档请求
 */
@Getter
@Setter
@Serdeable
public class DocAddRequest {
    /**
     * 文档名称
     */
    private String name;

    /**
     * 文档内容
     */
    private String text;

    /**
     * 文档类型
     */
    private Integer type;

    /**
     * 用户ID（文档所有者）
     */
    private String userId;
}

