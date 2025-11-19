package com.example.ddd.infrastructure.dao.po;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;
import org.jooq.JSONB;

/**
 * 向量文档持久化对象
 * 注意：由于vector_document表使用pgvector类型，暂时不继承JOOQ生成的Record
 * 等JOOQ支持pgvector后可以改为继承VectorDocumentRecord
 */
@Getter
@Setter
@Serdeable
public class VectorDocumentPO {
    private String embeddingId;
    private Long ragId;
    private String content;
    private float[] embedding; // 向量数组
    private JSONB metadata; // 元数据JSONB
    private Integer chunkIndex;
    private Long createdAt;
    private Long updatedAt;
}

