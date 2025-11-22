package com.example.ddd.domain.agent.model.valobj;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationDTO {

    @JsonProperty("subject_name")
    private String subjectName;

    @JsonProperty("subject_type")
    private String subjectType;

    @JsonProperty("relation_type")
    private String relationType;

    @JsonProperty("object_name")
    private String objectName;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("evidence_text")
    private String evidenceText;

    public RelationDTO() {
    }

    public RelationDTO(String subjectName,
                       String subjectType,
                       String relationType,
                       String objectName,
                       String objectType,
                       String evidenceText) {
        this.subjectName = subjectName;
        this.subjectType = subjectType;
        this.relationType = relationType;
        this.objectName = objectName;
        this.objectType = objectType;
        this.evidenceText = evidenceText;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(String subjectType) {
        this.subjectType = subjectType;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getEvidenceText() {
        return evidenceText;
    }

    public void setEvidenceText(String evidenceText) {
        this.evidenceText = evidenceText;
    }
}
