package com.example.ddd.domain.agent.service.execute.task;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Serdeable
public class Task {
    String id;
    String type;
    String assignee;
    String payload;
}
