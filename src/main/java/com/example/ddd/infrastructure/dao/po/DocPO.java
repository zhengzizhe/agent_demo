package com.example.ddd.infrastructure.dao.po;

import com.example.jooq.tables.records.DocRecord;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * 文档持久化对象
 */
@Getter
@Setter
@Serdeable
public class DocPO extends DocRecord {
}
