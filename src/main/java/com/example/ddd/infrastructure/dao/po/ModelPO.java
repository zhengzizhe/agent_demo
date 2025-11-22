package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * Model持久化对象
 */
@Getter
@Setter
public class ModelPO {
    private String id;
    private String name;
    private String provider;
    private String modelType;
    private String apiEndpoint;
    private String apiKey;
    private Integer maxTokens;
    private Double temperature;
    private String status;
    private Long createdAt;
    private Long updatedAt;
}


































