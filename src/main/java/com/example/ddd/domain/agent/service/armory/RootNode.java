package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.domain.agent.model.entity.*;
import com.example.ddd.infrastructure.adapter.repository.AgentRepository;
import com.example.ddd.infrastructure.adapter.repository.ClientRepository;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import dev.langchain4j.internal.Json;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletableFuture;

import static com.example.ddd.common.constant.IAgentConstant.*;

@Slf4j
@Singleton
public class RootNode extends AbstractArmorySupport {
    @Inject
    private AgentRepository agentRepository;
    @Inject
    private ClientRepository clientRepository;
    @Inject
    private DSLContextFactory dslContextFactory;
    @Inject
    RagNode ragNode;


    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        log.info("Ai agent开始构建流程:{}", "默认配置");
        Long agentId = armoryCommandEntity.getAgentId();

        CompletableFuture<Void> agentFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            AgentEntity agent = agentRepository.queryById(dslContext, agentId);
            dynamicContext.put(AGENT_KEY, Json.toJson(agent));
            log.info("AI agent构建中 查找到agent:{}，数量为:{}", Json.toJson(agent), agent == null ? 0 : 1);
        }));

        CompletableFuture<Void> clientFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            List<ClientEntity> clients = clientRepository.queryByAgentId(dslContext, agentId);
            dynamicContext.put(CLIENT_KEY, Json.toJson(clients));
            log.info("AI agent构建中 获取client信息: {},数量为:{}", Json.toJson(clients), clients.size());
        }));

        CompletableFuture<Void> modelMapFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            Map<Long, List<ChatModelEntity>> modelMap = clientRepository.queryModelMapByAgentId(dslContext, agentId);

            dynamicContext.put(MODEL_KEY, Json.toJson(modelMap));
            log.info("AI agent构建中 获取model信息: {},数量为:{}", Json.toJson(modelMap), modelMap.size());
        }));

        CompletableFuture<Void> ragMapFuture = CompletableFuture.runAsync(() -> dslContextFactory.execute(dslContext -> {
            Map<Long, List<RagEntity>> ragMap = clientRepository.queryRagMapByAgentId(dslContext, agentId);

            dynamicContext.put(RAG_KEY, Json.toJson(ragMap));
            log.info("AI agent构建中 获取Rag信息: {},数量为:{}", Json.toJson(ragMap), ragMap.size());
        }));

        CompletableFuture.allOf(agentFuture, clientFuture, modelMapFuture, ragMapFuture).join();

        return router(armoryCommandEntity, dynamicContext);
    }
 

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return ragNode;
    }
}
