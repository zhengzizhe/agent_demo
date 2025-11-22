package com.example.ddd.infrastructure.dao.po;

import lombok.Getter;
import lombok.Setter;

/**
 * 文档持久化对象
 */
@Getter
@Setter
public class DocPO {
    private String id;
    private String name;
    private String owner;
    private String text;
    private Integer type;
}
