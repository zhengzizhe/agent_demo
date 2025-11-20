package com.example.ddd.domain.agent.model.valobj;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

@Serdeable
@Getter
@Setter
public class EntityDTO {

    // name: 实体名称
    private String name;

    // type: 实体类型（Tech/System/Person/...）
    private String type;

    // description: 实体简短描述
    private String description;

    public EntityDTO() {
    }

    public EntityDTO(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
