package com.example.ddd.trigger.request;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * RAG搜索请求
 */
@Getter
@Setter
@Serdeable
public class RagSearchRequest {
    /**
     * RAG ID
     */
    private Long ragId;
    
    /**
     * 查询文本
     */
    private String queryText;
    
    /**
     * 返回结果数量限制（默认10）
     */
    private Integer limit = 10;
    
    /**
     * 相似度阈值（默认0.6）
     */
    private Double similarityThreshold = 0.6;
}










