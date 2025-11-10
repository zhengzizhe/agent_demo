package com.example.ddd.domain.agent.service.execute.task;

import java.util.List;

public interface TaskPlanner {
    List<Task> fromSupervisorPlanOrFallback(String planJsonOrNull);
}