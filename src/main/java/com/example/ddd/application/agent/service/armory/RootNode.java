package com.example.ddd.application.agent.service.armory;

import com.example.ddd.application.agent.repository.IAgentRepository;
import com.example.ddd.application.agent.repository.IOrchestratorRepository;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.domain.agent.model.entity.*;
import dev.langchain4j.internal.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.ddd.common.constant.IAgentConstant.*;


@Service
@Slf4j
public class RootNode extends AbstractArmorySupport {

    @Autowired
    McpNode mcpNode;
    @Autowired
    private IAgentRepository agentRepository;
    @Autowired
    private IOrchestratorRepository orchestratorRepository;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        String orchestratorId = String.valueOf(armoryCommandEntity.getOrchestratorId());
        log.info("多agent构建中: orchestratorId={}", orchestratorId);

        CompletableFuture<Void> agentFuture = CompletableFuture.runAsync(() -> {
            OrchestratorEntity orchestrator = orchestratorRepository.queryById(orchestratorId);
            dynamicContext.put(AGENT_KEY, Json.toJson(orchestrator));
            log.info("多agent构建中 查询orchestrator信息: orchestratorId={}, 已找到={}", orchestratorId, orchestrator != null);
        });

        CompletableFuture<Void> clientFuture = CompletableFuture.runAsync(() -> {
            List<AgentEntity> agents = agentRepository.queryByOrchestratorId(orchestratorId);
            dynamicContext.put(CLIENT_KEY, Json.toJson(agents));
            log.info("多agent构建中 查询agent列表: orchestratorId={}, agent数量={}", orchestratorId, agents.size());
        });

        CompletableFuture<Void> modelMapFuture = CompletableFuture.runAsync(() -> {
            Map<String, ChatModelEntity> modelMap = agentRepository.queryModelMapByOrchestratorId(orchestratorId);
            dynamicContext.put(MODEL_KEY, Json.toJson(modelMap));
            log.info("多agent构建中 查询model配置: orchestratorId={}, agent数量={}", orchestratorId, modelMap.size());
        });

        CompletableFuture<Void> ragMapFuture = CompletableFuture.runAsync(() -> {
            Map<String, List<RagEntity>> ragMap = agentRepository.queryRagMapByOrchestratorId(orchestratorId);
            dynamicContext.put(RAG_KEY, Json.toJson(ragMap));
            log.info("多agent构建中 查询RAG配置: orchestratorId={}, agent数量={}", orchestratorId, ragMap.size());
        });

        CompletableFuture<Void> mcpMapFuture = CompletableFuture.runAsync(() -> {
            Map<String, List<McpEntity>> mcpMap = agentRepository.queryMcpMapByOrchestratorId(orchestratorId);
            dynamicContext.put(MCP_KEY, Json.toJson(mcpMap));
            log.info("多agent构建中 查询MCP配置: orchestratorId={}, agent数量={}", orchestratorId, mcpMap.size());
        });

        CompletableFuture.allOf(agentFuture, clientFuture, modelMapFuture, ragMapFuture, mcpMapFuture).join();

        return router(armoryCommandEntity, dynamicContext);
    }


    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return mcpNode;
    }
}
