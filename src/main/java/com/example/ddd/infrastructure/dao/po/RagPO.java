package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * RAG持久化对象
 */
@Getter
@Setter
public class RagPO {
    private String id;
    private String name;
    private String vectorStoreType;
    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String databaseType;
    private String databaseHost;
    private Integer databasePort;
    private String databaseName;
    private String databaseUser;
    private String databasePassword;
    private String tableName;
    private Integer dimension;
    private Boolean useIndex;
    private Integer indexListSize;
    private String config; // JSON字符串
    private String status;
    private Long createdAt;
    private Long updatedAt;
}



































