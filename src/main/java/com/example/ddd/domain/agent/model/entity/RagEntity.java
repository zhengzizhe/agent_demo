package com.example.ddd.domain.agent.model.entity;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * RAG实体
 */
@Getter
@Setter
@Serdeable
public class RagEntity {
    private Long id;
    private Long clientId;
    private String name;
    private String vectorStoreType;
    private String embeddingModel;
    private Integer chunkSize;
    private Integer chunkOverlap;

    // 数据库连接配置
    private String databaseType;      // 数据库类型：POSTGRESQL, MYSQL等
    private String databaseHost;       // 数据库主机地址
    private Integer databasePort;      // 数据库端口
    private String databaseName;       // 数据库名称
    private String databaseUser;       // 数据库用户名
    private String databasePassword;   // 数据库密码

    // 向量存储配置
    private String tableName;          // 向量存储表名
    private Integer dimension;         // 向量维度
    private Boolean useIndex;          // 是否使用索引
    private Integer indexListSize;     // 索引列表大小

    private Object config;             // 扩展配置（JSON格式）
    private String status;
    private Long createdAt;
    private Long updatedAt;
}

