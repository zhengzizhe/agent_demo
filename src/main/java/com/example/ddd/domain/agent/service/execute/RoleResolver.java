package com.example.ddd.domain.agent.service.execute;

import com.example.ddd.domain.agent.model.entity.ClientEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色解析器
 * 简化版：seq=1的Client作为主管，其他按顺序分类
 */
@Slf4j
public class RoleResolver {

    public static class RoleAssignment {
        private Long supervisorClientId;
        private Long supervisorModelId;
        private List<WorkerInfo> researchers = new ArrayList<>();
        private List<WorkerInfo> executors = new ArrayList<>();

        public Long getSupervisorClientId() {
            return supervisorClientId;
        }

        public void setSupervisorClientId(Long supervisorClientId) {
            this.supervisorClientId = supervisorClientId;
        }

        public Long getSupervisorModelId() {
            return supervisorModelId;
        }

        public void setSupervisorModelId(Long supervisorModelId) {
            this.supervisorModelId = supervisorModelId;
        }

        public List<WorkerInfo> getResearchers() {
            return researchers;
        }

        public List<WorkerInfo> getExecutors() {
            return executors;
        }
    }

    public static class WorkerInfo {
        private Long clientId;
        private Long modelId;
        private List<Long> ragIds;

        public WorkerInfo(Long clientId, Long modelId, List<Long> ragIds) {
            this.clientId = clientId;
            this.modelId = modelId;
            this.ragIds = ragIds != null ? ragIds : new ArrayList<>();
        }

        public Long getClientId() {
            return clientId;
        }

        public Long getModelId() {
            return modelId;
        }

        public List<Long> getRagIds() {
            return ragIds;
        }
    }

    /**
     * 解析角色分配
     * 规则：seq=1的Client作为主管，其他按顺序分类（第2个Researcher，第3个Executor，第4个Critic）
     */
    public RoleAssignment resolveRoles(List<ClientEntity> clients,
                                       java.util.Map<Long, List<com.example.ddd.domain.agent.model.entity.ChatModelEntity>> clientModelMap,
                                       java.util.Map<Long, List<com.example.ddd.domain.agent.model.entity.RagEntity>> clientRagMap) {
        RoleAssignment assignment = new RoleAssignment();

        if (clients == null || clients.isEmpty()) {
            throw new IllegalArgumentException("没有配置Client，无法解析角色");
        }

        // 主管必须是第一个（seq=1）
        ClientEntity supervisorClient = clients.get(0);
        Long supervisorClientId = supervisorClient.getId();
        
        List<com.example.ddd.domain.agent.model.entity.ChatModelEntity> supervisorModels = clientModelMap.get(supervisorClientId);
        if (supervisorModels == null || supervisorModels.isEmpty()) {
            throw new IllegalArgumentException("主管Client（seq=1）没有配置Model: clientId=" + supervisorClientId);
        }
        
        Long supervisorModelId = supervisorModels.get(0).getId();
        assignment.setSupervisorClientId(supervisorClientId);
        assignment.setSupervisorModelId(supervisorModelId);
        log.info("主管: clientId={}, modelId={}", supervisorClientId, supervisorModelId);
        for (int i = 1; i < clients.size(); i++) {
            ClientEntity client = clients.get(i);
            List<com.example.ddd.domain.agent.model.entity.ChatModelEntity> models = clientModelMap.get(client.getId());
            if (models == null || models.isEmpty()) {
                log.warn("Client {} 没有配置Model，跳过", client.getId());
                continue;
            }
            Long modelId = models.get(0).getId();
            List<Long> ragIds = extractRagIds(clientRagMap.get(client.getId()));
            WorkerInfo worker = new WorkerInfo(client.getId(), modelId, ragIds);
            if (i == 1) {
                assignment.getResearchers().add(worker);
                log.info("Researcher: clientId={}, modelId={}", client.getId(), modelId);
            } else if (i == 2) {
                assignment.getExecutors().add(worker);
                log.info("Executor: clientId={}, modelId={}", client.getId(), modelId);
            } else {
                // 第4个及以后默认Executor
                assignment.getExecutors().add(worker);
                log.info("Executor（默认）: clientId={}, modelId={}", client.getId(), modelId);
            }
        }
        return assignment;
    }

    private List<Long> extractRagIds(List<com.example.ddd.domain.agent.model.entity.RagEntity> rags) {
        if (rags == null || rags.isEmpty()) {
            return new ArrayList<>();
        }
        return rags.stream()
                .map(com.example.ddd.domain.agent.model.entity.RagEntity::getId)
                .collect(java.util.stream.Collectors.toList());
    }
}

