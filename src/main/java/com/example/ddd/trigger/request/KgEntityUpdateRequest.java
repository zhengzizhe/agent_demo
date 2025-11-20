package com.example.ddd.trigger.request;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新知识图谱实体请求
 */
@Getter
@Setter
@Serdeable
public class KgEntityUpdateRequest {
    /**
     * 实体名称
     */
    private String name;

    /**
     * 实体类型
     */
    private String type;

    /**
     * 实体描述
     */
    private String description;

    /**
     * 实体向量嵌入（1536维）
     */
    private float[] embedding;
}

