package com.example.ddd.domain.agent.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 向量文档实体
 */
@Getter
@Setter
public class VectorDocumentEntity {
    private String embeddingId;
    private String ragId;
    private String text;
    private float[] embedding; // 向量数组
    private Map<String, Object> metadata; // 元数据
    private Integer chunkIndex;
    private Long createdAt;
    private Long updatedAt;
}




































