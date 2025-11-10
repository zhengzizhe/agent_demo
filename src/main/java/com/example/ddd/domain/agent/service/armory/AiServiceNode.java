package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.*;
import com.example.ddd.domain.agent.service.execute.AgentOrchestrator;
import com.example.ddd.domain.agent.service.execute.AiServiceFactory;
import com.example.ddd.domain.agent.service.execute.CriticService.CriticService;
import com.example.ddd.domain.agent.service.execute.ExecutorService.ExecutorService;
import com.example.ddd.domain.agent.service.execute.ResearcherService.ResearcherService;
import com.example.ddd.domain.agent.service.execute.RoleResolver;
import com.example.ddd.domain.agent.service.execute.SupervisorService.SupervisorService;
import com.example.ddd.domain.agent.service.execute.blackBoard.InMemoryBlackboard;
import com.example.ddd.domain.agent.service.execute.task.DbSequencePlanner;
import dev.langchain4j.model.chat.StreamingChatModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.*;

@Slf4j
@Singleton
public class AiServiceNode extends AbstractArmorySupport {
    @Inject
    BeanUtil beanUtil;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        Long agentId = armoryCommandEntity.getAgentId();
        log.info("Ai agent构建中 构建agentId:{}", agentId);
        // 获取 clients
        String clientJson = dynamicContext.get(CLIENT_KEY);
        List<ClientEntity> clients = JSON.parseListObject(clientJson, ClientEntity.class);
        if (clients == null || clients.isEmpty()) {
            log.warn("Agent {} 没有配置Client，无法构建AIService", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        String chatModelJson = dynamicContext.get(MODEL_KEY);
        Map<Long, List<ChatModelEntity>> modelMap = JSON.parseObject(chatModelJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<ChatModelEntity>>>() {
                });
        if (modelMap == null || modelMap.isEmpty()) {
            log.warn("Agent {} 没有配置Model，无法构建AIService", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        String ragJson = dynamicContext.get(RAG_KEY);
        Map<Long, List<RagEntity>> ragMap = JSON.parseObject(ragJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<RagEntity>>>() {
                });

        // 获取Agent实体（用于角色解析）
        String agentJson = dynamicContext.get(AGENT_KEY);
        AgentEntity agent = JSON.parseObject(agentJson, AgentEntity.class);
        // 2. 构建Orchestrator（新增逻辑）
        try {
            buildOrchestrator(agentId, agent, clients, modelMap, ragMap);
        } catch (Exception e) {
            log.error("构建Orchestrator失败: agentId={}, error={}", agentId, e.getMessage(), e);
        }
        log.info("Ai agent构建完成 agentId:{}", agentId);
        return router(armoryCommandEntity, dynamicContext);
    }

    /**
     * 构建AgentOrchestrator
     * 为每个Client构建一个Orchestrator实例
     */
    private void buildOrchestrator(Long agentId, AgentEntity agent,
                                   List<ClientEntity> clients,
                                   Map<Long, List<ChatModelEntity>> modelMap,
                                   Map<Long, List<RagEntity>> ragMap) {
        if (clients == null || clients.isEmpty()) {
            log.warn("没有配置Client，跳过Orchestrator构建");
            return;
        }

        RoleResolver roleResolver = new RoleResolver();
        RoleResolver.RoleAssignment assignment;
        try {
            assignment = roleResolver.resolveRoles(clients, modelMap, ragMap);
        } catch (IllegalArgumentException e) {
            log.error("角色解析失败: {}", e.getMessage());
            return;
        }
        log.info("角色解析结果: supervisorClientId={}, supervisorModelId={}, researchers={}, executors={}",
                assignment.getSupervisorClientId(), assignment.getSupervisorModelId(),
                assignment.getResearchers().size(), assignment.getExecutors().size());
        StreamingChatModel supervisorModel = beanUtil.getChatModel(assignment.getSupervisorModelId());
        if (supervisorModel == null) {
            log.error("主管模型不存在: modelId={}", assignment.getSupervisorModelId());
            return;
        }
        StreamingChatModel researcherModel = null;
        StreamingChatModel executorModel = null;
        if (!assignment.getResearchers().isEmpty()) {
            Long researcherModelId = assignment.getResearchers().get(0).getModelId();
            researcherModel = beanUtil.getChatModel(researcherModelId);
        }
        if (researcherModel == null) {
            researcherModel = supervisorModel; // 兜底使用主管模型
        }
        if (!assignment.getExecutors().isEmpty()) {
            Long executorModelId = assignment.getExecutors().get(0).getModelId();
            executorModel = beanUtil.getChatModel(executorModelId);
        }
        if (executorModel == null) {
            executorModel = supervisorModel;
        }
        ClientEntity supervisorClient = clients.stream()
                .filter(c -> c.getId().equals(assignment.getSupervisorClientId()))
                .findFirst()
                .orElse(null);
        if (supervisorClient == null) {
            log.warn("主管Client不存在: clientId={}", assignment.getSupervisorClientId());
            return;
        }
        
        // 查找Researcher Client
        ClientEntity researcherClient = null;
        if (!assignment.getResearchers().isEmpty()) {
            Long researcherClientId = assignment.getResearchers().get(0).getClientId();
            researcherClient = clients.stream()
                    .filter(c -> c.getId().equals(researcherClientId))
                    .findFirst()
                    .orElse(null);
        }
        if (researcherClient == null) {
            log.warn("Researcher Client不存在，使用Supervisor Client");
            researcherClient = supervisorClient;
        }
        
        // 查找Executor Client
        ClientEntity executorClient = null;
        if (!assignment.getExecutors().isEmpty()) {
            Long executorClientId = assignment.getExecutors().get(0).getClientId();
            executorClient = clients.stream()
                    .filter(c -> c.getId().equals(executorClientId))
                    .findFirst()
                    .orElse(null);
        }
        if (executorClient == null) {
            log.warn("Executor Client不存在，使用Supervisor Client");
            executorClient = supervisorClient;
        }
        
        // 查找Critic Client（通常是第4个，seq=4）
        ClientEntity criticClient = null;
        if (clients.size() >= 4) {
            criticClient = clients.get(3); // 第4个（索引3）
        }
        if (criticClient == null) {
            log.warn("Critic Client不存在，使用Supervisor Client");
            criticClient = supervisorClient;
        }
        
        AiServiceFactory factory = new AiServiceFactory(beanUtil);
        SupervisorService supervisor =
                factory.buildSupervisor(supervisorModel, supervisorClient);
        List<Long> allRagIds = new ArrayList<>();
        assignment.getResearchers().forEach(w -> allRagIds.addAll(w.getRagIds()));
        if (allRagIds.isEmpty() && ragMap != null) {
            // 如果没有明确的RAG，使用所有Client的RAG
            ragMap.values().forEach(rags ->
                    rags.forEach(rag -> allRagIds.add(rag.getId()))
            );
        }
        ResearcherService researcher =
                factory.buildResearcher(researcherModel, researcherClient, allRagIds);
        ExecutorService executor =
                factory.buildExecutor(executorModel, executorClient);
        CriticService critic =
                factory.buildCritic(supervisorModel, criticClient);
        com.example.ddd.domain.agent.service.execute.blackBoard.InMemoryBlackboard board =
                new InMemoryBlackboard();
        DbSequencePlanner planner = new DbSequencePlanner(agentId, clients);
        AgentOrchestrator orchestrator = new AgentOrchestrator(
                supervisor,
                researcher,
                executor,
                critic,
                planner
        );
        beanUtil.registerOrchestrator(agentId, orchestrator);
        log.info("Orchestrator构建完成: agentId={}", agentId);
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return null;
    }
}
