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
    protected final UserContext userContext;

    public BaseTaskExecutor(Task task, ServiceNode serviceNode, UserContext userContext) {
        this.task = task;
        this.serviceNode = serviceNode;
        this.userContext = userContext;
    }
    
    /**
     * 获取用户上下文
     */
    protected UserContext getUserContext() {
        return userContext;
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
     * 返回Map结构，包含usermessage和taskInput字段
     */
    protected Map<String, Object> getTaskInput(WorkspaceState state) {
        String userMessage = getUserInput(state);
        Map<String, Object> inputMap = new HashMap<>();
        
        // 始终添加用户消息
        inputMap.put("usermessage", userMessage != null ? userMessage : "");
        
        // 获取任务特定的输入
        String taskSpecificInput = "";
        if (task.getInputs() != null) {
            if (task.getInputs().getFromUser() != null && task.getInputs().getFromUser()) {
                // 如果任务明确要求从用户输入获取，使用用户输入
                taskSpecificInput = userMessage;
            } else if (task.getInputs().getFromTask() != null) {
                // 如果任务依赖其他任务的输出
                String fromTaskId = task.getInputs().getFromTask();
                Optional<Map<String, Object>> scratchpad = state.scratchpad();
                if (scratchpad.isPresent()) {
                    Map<String, Object> data = scratchpad.get();
                    Object taskResult = data.get("task_" + fromTaskId);
                    if (taskResult != null) {
                        taskSpecificInput = taskResult.toString();
                    }
                }
            }
        }
        
        // 如果没有获取到任务特定输入，使用用户输入作为默认值
        if (taskSpecificInput == null || taskSpecificInput.trim().isEmpty()) {
            taskSpecificInput = userMessage;
        }
        
        // 添加任务特定输入
        inputMap.put("taskInput", taskSpecificInput != null ? taskSpecificInput : "");
        
        return inputMap;
    }
    
    /**
     * 获取格式化的任务输入字符串（用于向后兼容）
     * 格式：usermessage:用户问题\n[任务输入内容]
     */
    protected String getTaskInputString(WorkspaceState state) {
        Map<String, Object> inputMap = getTaskInput(state);
        StringBuilder inputBuilder = new StringBuilder();
        
        String userMessage = (String) inputMap.getOrDefault("usermessage", "");
        String taskInput = (String) inputMap.getOrDefault("taskInput", "");
        
        // 始终添加用户消息
        if (userMessage != null && !userMessage.trim().isEmpty()) {
            inputBuilder.append("usermessage:").append(userMessage).append("\n");
        }
        
        // 添加任务特定输入
        if (taskInput != null && !taskInput.trim().isEmpty()) {
            inputBuilder.append(taskInput);
        }
        
        return inputBuilder.toString();
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

