package com.example.ddd.interfaces.response;

import lombok.*;

/**
 * 知识图谱实体视图
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KgEntityView {
    private String id;
    private String name;
    private String type;
    private String description;
}

