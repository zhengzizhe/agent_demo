package com.example.ddd.interfaces.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 添加知识图谱实体请求
 */
@Getter
@Setter
public class KgEntityAddRequest {
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

