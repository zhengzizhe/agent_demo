package com.example.ddd.trigger.response;

import io.micronaut.serde.annotation.Serdeable;
import lombok.*;

/**
 * 知识图谱实体视图
 */
@Serdeable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KgEntityView {
    private Long id;
    private String name;
    private String type;
    private String description;
}

