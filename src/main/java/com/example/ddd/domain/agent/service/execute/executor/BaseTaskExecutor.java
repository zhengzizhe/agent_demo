package com.example.ddd.domain.agent.service.execute.executor;


import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.context.UserContext;
import com.example.ddd.domain.agent.service.execute.graph.WorkspaceState;
import com.example.ddd.domain.agent.service.execute.task.Task;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 基础任务执行器
 */
@Slf4j
@Getter
public abstract class BaseTaskExecutor implements TaskExecutor {
    protected final Task task;
    protected final ServiceNode serviceNode;

    public BaseTaskExecutor(Task task, ServiceNode serviceNode) {
        this.task = task;
        this.serviceNode = serviceNode;
    }
    
    /**
     * 获取用户上下文（从 ThreadLocal 获取）
     */
    protected UserContext getUserContext() {
        return UserContext.get();
    }

    /**
     * 获取用户输入文本
     */
    protected String getUserInput(WorkspaceState state) {
        Optional<String> s = state.userMessage();
        return s.orElse("");
    }

    /**
     * 获取任务输入（从state或依赖任务）
     */
    protected String getTaskInput(WorkspaceState state) {
        if (task.getInputs() == null) {
            return getUserInput(state);
        }
        if (task.getInputs().getFromUser() != null && task.getInputs().getFromUser()) {
            return getUserInput(state);
        }
        if (task.getInputs().getFromTask() != null) {
            String fromTaskId = task.getInputs().getFromTask();
            Optional<Map<String, Object>> scratchpad = state.scratchpad();
            if (scratchpad.isPresent()) {
                Map<String, Object> data = scratchpad.get();
                Object taskResult = data.get("task_" + fromTaskId);
                if (taskResult != null) {
                    return taskResult.toString();
                }
            }
        }

        return getUserInput(state);
    }

    /**
     * 保存任务结果到state
     */
    protected Map<String, Object> saveTaskResult(WorkspaceState state, String result) {
        Map<String, Object> scratchpad = new HashMap<>(state.scratchpad().orElse(new HashMap<>()));
        scratchpad.put("task_" + task.getId(), result);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("scratchpad", scratchpad);
        return resultMap;
    }
}

