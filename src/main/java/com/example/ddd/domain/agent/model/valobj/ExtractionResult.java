package com.example.ddd.domain.agent.model.valobj;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Serdeable
@Getter
@Setter
public class ExtractionResult {

    private List<EntityDTO> entities;
    private List<RelationDTO> relations;

    public ExtractionResult() {
    }

    public ExtractionResult(List<EntityDTO> entities, List<RelationDTO> relations) {
        this.entities = entities;
        this.relations = relations;
    }

    public List<EntityDTO> getEntities() {
        return entities;
    }

    public void setEntities(List<EntityDTO> entities) {
        this.entities = entities;
    }

    public List<RelationDTO> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationDTO> relations) {
        this.relations = relations;
    }
}
