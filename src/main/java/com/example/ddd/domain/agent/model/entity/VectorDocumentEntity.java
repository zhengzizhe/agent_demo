package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 向量文档实体
 */
@Getter
@Setter
@Serdeable
public class VectorDocumentEntity {
    private Long id;
    private Long ragId;
    private String content;
    private float[] embedding; // 向量数组
    private Map<String, Object> metadata; // 元数据
    private Integer chunkIndex;
    private Long createdAt;
    private Long updatedAt;
}















