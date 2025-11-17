package com.example.ddd.trigger.request;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * RAG添加文档请求
 */
@Getter
@Setter
@Serdeable
public class RagAddDocumentRequest {
    /**
     * RAG ID（保留参数，但使用硬编码配置）
     */
    private Long ragId;
    
    /**
     * 文档文本
     */
    private String text;
    
    /**
     * 文档元数据
     */
    private Map<String, Object> metadata;
}











