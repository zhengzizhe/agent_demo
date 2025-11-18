package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.domain.agent.adapter.repository.IOrchestratorRepository;
import com.example.ddd.domain.agent.adapter.repository.IAgentRepository;
import com.example.ddd.domain.agent.model.entity.*;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import dev.langchain4j.internal.Json;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.example.ddd.common.constant.IAgentConstant.*;


@Singleton
@Slf4j
public class RootNode extends AbstractArmorySupport {

    @Inject
    private IAgentRepository agentRepository;
    @Inject
    private DSLContextFactory dslContextFactory;
    @Inject
    private IOrchestratorRepository orchestratorRepository;
    @Inject
    McpNode mcpNode;


    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        Long orchestratorId = armoryCommandEntity.getOrchestratorId();
        log.info("多agent构建中: orchestratorId={}", orchestratorId);

        CompletableFuture<Void> agentFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            OrchestratorEntity orchestrator = orchestratorRepository.queryById(dslContext, orchestratorId);
            dynamicContext.put(AGENT_KEY, Json.toJson(orchestrator));
            log.info("多agent构建中 查询orchestrator信息: orchestratorId={}, 已找到={}", orchestratorId, orchestrator != null);
        }));

        CompletableFuture<Void> clientFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            List<AgentEntity> agents = agentRepository.queryByOrchestratorId(dslContext, orchestratorId);
            dynamicContext.put(CLIENT_KEY, Json.toJson(agents));
            log.info("多agent构建中 查询agent列表: orchestratorId={}, agent数量={}", orchestratorId, agents.size());
        }));

        CompletableFuture<Void> modelMapFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            Map<Long, ChatModelEntity> modelMap = agentRepository.queryModelMapByOrchestratorId(dslContext, orchestratorId);
            dynamicContext.put(MODEL_KEY, Json.toJson(modelMap));
            log.info("多agent构建中 查询model配置: orchestratorId={}, agent数量={}", orchestratorId, modelMap.size());
        }));

        CompletableFuture<Void> ragMapFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            Map<Long, List<RagEntity>> ragMap = agentRepository.queryRagMapByOrchestratorId(dslContext, orchestratorId);
            dynamicContext.put(RAG_KEY, Json.toJson(ragMap));
            log.info("多agent构建中 查询RAG配置: orchestratorId={}, agent数量={}", orchestratorId, ragMap.size());
        }));

        CompletableFuture<Void> mcpMapFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            Map<Long, List<McpEntity>> mcpMap = agentRepository.queryMcpMapByOrchestratorId(dslContext, orchestratorId);
            dynamicContext.put(MCP_KEY, Json.toJson(mcpMap));
            log.info("多agent构建中 查询MCP配置: orchestratorId={}, agent数量={}", orchestratorId, mcpMap.size());
        }));

        CompletableFuture.allOf(agentFuture, clientFuture, modelMapFuture, ragMapFuture, mcpMapFuture).join();

        return router(armoryCommandEntity, dynamicContext);
    }


    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return mcpNode;
    }
}
