package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 向量文档持久化对象
 */
@Getter
@Setter
public class VectorDocumentPO {
    private String embeddingId;
    private String ragId;
    private String content;
    private float[] embedding; // 向量数组
    private String metadata; // 元数据JSON字符串
    private Integer chunkIndex;
    private Long createdAt;
    private Long updatedAt;
}

