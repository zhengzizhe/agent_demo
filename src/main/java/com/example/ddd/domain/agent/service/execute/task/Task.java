// com/example/ddd/domain/agent/runtime/Task.java
package com.example.ddd.domain.agent.service.execute.task;

public record Task(String id, String type, String assignee, String payload) {
}
