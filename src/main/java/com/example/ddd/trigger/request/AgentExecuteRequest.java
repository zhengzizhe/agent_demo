package com.example.ddd.trigger.request;


import io.micronaut.serde.annotation.Serdeable;
import lombok.Getter;
import lombok.Setter;

/**
 * Agent执行请求
 */
@Getter
@Setter
@Serdeable
public class AgentExecuteRequest {
    private Long agentId;
    private String message;

}