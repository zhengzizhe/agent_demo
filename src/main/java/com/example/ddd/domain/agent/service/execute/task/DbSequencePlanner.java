package com.example.ddd.domain.agent.service.execute.task;

import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.common.utils.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务规划器实现
 * 支持从Supervisor计划解析或数据库顺序生成任务列表
 */
@Slf4j
public class DbSequencePlanner implements TaskPlanner {

    private List<ClientEntity> clients;

    public DbSequencePlanner(Long agentId, List<ClientEntity> clients) {
        this.clients = clients != null ? clients : new ArrayList<>();
    }

    @Override
    public List<Task> fromSupervisorPlanOrFallback(String planOrNull) {
        if (planOrNull != null && planOrNull.trim().startsWith("{")) {
            return parseFromPlan(planOrNull);
        }
        // 兜底：按数据库顺序执行
        return parseFromDbSequence();
    }

    /**
     * 解析Supervisor的计划JSON
     * 格式: {"tasks":[{"id":"t1","type":"research","assignee":"Researcher","payload":"{...}"}]}
     */
    private List<Task> parseFromPlan(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> planMap = JSON.parseObject(json, Map.class);
            if (planMap == null || !planMap.containsKey("tasks")) {
                log.warn("计划JSON格式错误，缺少tasks字段");
                return parseFromDbSequence();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> tasksList = (List<Map<String, Object>>) planMap.get("tasks");
            if (tasksList == null || tasksList.isEmpty()) {
                log.warn("计划中没有任务，使用数据库顺序");
                return parseFromDbSequence();
            }

            List<Task> tasks = new ArrayList<>();
            for (Map<String, Object> taskMap : tasksList) {
                String id = (String) taskMap.get("id");
                String type = (String) taskMap.get("type");
                String assignee = (String) taskMap.get("assignee");
                Object payloadObj = taskMap.get("payload");
                String payload = payloadObj instanceof String 
                    ? (String) payloadObj 
                    : JSON.toJSON(payloadObj);

                if (id != null && type != null && assignee != null && payload != null) {
                    tasks.add(new Task(id, type, assignee, payload));
                } else {
                    log.warn("任务格式不完整，跳过: {}", taskMap);
                }
            }

            log.info("从Supervisor计划解析出{}个任务", tasks.size());
            return tasks;
        } catch (Exception e) {
            log.error("解析Supervisor计划失败，使用数据库顺序: {}", e.getMessage());
            return parseFromDbSequence();
        }
    }

    /**
     * 从数据库顺序生成任务列表
     * 按照agent_client表的seq顺序，将clients转换为Task
     */
    private List<Task> parseFromDbSequence() {
        List<Task> tasks = new ArrayList<>();
        
        if (clients == null || clients.isEmpty()) {
            log.warn("没有配置Client，无法生成任务");
            return tasks;
        }

        // 跳过第一个（主管），从第二个开始
        for (int i = 1; i < clients.size(); i++) {
            ClientEntity client = clients.get(i);
            String taskId = "t" + (i - 1);
            
            // 根据Client的能力判断任务类型和分配者
            String type = determineTaskType(client);
            String assignee = determineAssignee(client, type);
            
            // 构建payload
            Map<String, Object> payloadMap = new HashMap<>();
            payloadMap.put("clientId", client.getId());
            payloadMap.put("clientName", client.getName());
            payloadMap.put("description", client.getDescription());
            payloadMap.put("systemPrompt", client.getSystemPrompt());
            
            String payload = JSON.toJSON(payloadMap);
            tasks.add(new Task(taskId, type, assignee, payload));
        }

        log.info("从数据库顺序生成{}个任务", tasks.size());
        return tasks;
    }

    /**
     * 判断任务类型：简化版，按顺序第2个research，第3个execute
     */
    private String determineTaskType(ClientEntity client) {
        // 简化：根据在clients中的位置判断
        int index = clients.indexOf(client);
        if (index == 1) {
            return "research";
        }
        return "execute";
    }

    /**
     * 判断分配者：简化版
     */
    private String determineAssignee(ClientEntity client, String type) {
        return "research".equals(type) ? "Researcher" : "Executor";
    }
}