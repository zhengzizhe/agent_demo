package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 知识图谱实体持久化对象
 */
@Getter
@Setter
public class KgEntityPO {
    private String id;
    private String name;
    private String type;
    private String description;
    private float[] embedding; // 向量数组
}

