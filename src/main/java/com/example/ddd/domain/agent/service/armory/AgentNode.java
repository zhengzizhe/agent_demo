package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.*;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.*;

@Slf4j
@Singleton
public class AgentNode extends AbstractArmorySupport {
    @Inject
    BeanUtil beanUtil;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        Long agentId = armoryCommandEntity.getOrchestratorId();
        log.info("多agent构建中 开始构建ServiceNode: orchestratorId={}", agentId);
        String clientJson = dynamicContext.get(CLIENT_KEY);
        List<AgentEntity> clients = JSON.parseListObject(clientJson, AgentEntity.class);
        if (clients == null || clients.isEmpty()) {
            log.warn("多agent构建中 orchestratorId={} 没有配置agent，跳过ServiceNode构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        String chatModelJson = dynamicContext.get(MODEL_KEY);
        Map<Long, List<ChatModelEntity>> modelMap = JSON.parseObject(chatModelJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<ChatModelEntity>>>() {
                });
        if (modelMap == null || modelMap.isEmpty()) {
            log.warn("多agent构建中 orchestratorId={} 没有配置Model，跳过ServiceNode构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        String ragJson = dynamicContext.get(RAG_KEY);
        Map<Long, List<RagEntity>> ragMap = JSON.parseObject(ragJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<RagEntity>>>() {
                });
        String agentJson = dynamicContext.get(AGENT_KEY);
        OrchestratorEntity agent = JSON.parseObject(agentJson, OrchestratorEntity.class);
        try {
            build(agent, clients, modelMap, ragMap);
        } catch (Exception e) {
            log.error("多agent构建中 构建ServiceNode失败: orchestratorId={}, error={}", agentId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.debug("多agent构建中 ServiceNode构建完成: orchestratorId={}", agentId);
        return router(armoryCommandEntity, dynamicContext);
    }

    private void build(OrchestratorEntity agent, List<AgentEntity> clients, Map<Long, List<ChatModelEntity>> modelMap, Map<Long, List<RagEntity>> ragMap) {
        Long agentId = agent.getId();
        log.info("多agent构建中 开始构建AiService: orchestratorId={}, agent数量={}", agentId, clients.size());
        for (AgentEntity client : clients) {
            try {
                // 1. 获取该agent对应的ChatModel列表
                List<ChatModelEntity> chatModels = modelMap.get(client.getId());
                if (chatModels == null || chatModels.isEmpty()) {
                    log.warn("多agent构建中 agentId={} 没有配置ChatModel，跳过AiService构建", client.getId());
                    continue;
                }
                ChatModelEntity chatModelEntity = chatModels.get(0);
                StreamingChatModel chatModel = beanUtil.getChatModel(chatModelEntity.getId());
                if (chatModel == null) {
                    log.warn("多agent构建中 ChatModel {} 未找到，跳过AiService构建: agentId={}", chatModelEntity.getId(), client.getId());
                    continue;
                }
                EmbeddingStore<?> embeddingStore = null;
                EmbeddingModel embeddingModel = null;
                List<RagEntity> rags = ragMap.get(client.getId());
                if (rags != null && !rags.isEmpty()) {
                    RagEntity ragEntity = rags.get(0);
                    embeddingStore = beanUtil.getEmbeddingStore(ragEntity.getId());
                    embeddingModel = beanUtil.getEmbeddingModel(ragEntity.getId());
                    if (embeddingStore == null) {
                        log.warn("多agent构建中 EmbeddingStore {} 未找到，将不使用RAG: agentId={}", ragEntity.getId(), client.getId());
                    }
                    if (embeddingModel == null) {
                        log.warn("多agent构建中 EmbeddingModel {} 未找到，RAG功能可能不完整: agentId={}", ragEntity.getId(), client.getId());
                    }
                }
                AiService aiService = buildAiService(client, chatModel, embeddingStore, embeddingModel);
                // buildAiService 现在会抛出异常而不是返回 null，所以这里不需要 null 检查
                beanUtil.registerAiService(client.getId(), aiService);
                ServiceNode serviceNode = buildServiceNode(client, aiService);
                if (serviceNode != null) {
                    beanUtil.registerServiceNode(agentId, serviceNode);
                    log.info("多agent构建中 ServiceNode构建并注册成功: orchestratorId={}, agentId={}, agentName={}, role={}",
                            agentId, client.getId(), client.getName(), client.getRole());
                } else {
                    log.warn("多agent构建中 ServiceNode创建失败: agentId={}", client.getId());
                }
            } catch (Exception e) {
                log.error("多agent构建中 构建AiService失败: agentId={}, error={}", client.getId(), e.getMessage(), e);
                throw new RuntimeException("构建AiService失败: agentId=" + client.getId(), e);
            }
        }

        log.info("多agent构建中 AiService构建完成: orchestratorId={}", agentId);
    }

    /**
     * 封装ServiceNode的创建
     *
     * @param client    Client实体
     * @param aiService AiService实例
     * @return ServiceNode实例
     */
    private ServiceNode buildServiceNode(AgentEntity client, AiService aiService) {
        if (client == null || aiService == null) {
            return null;
        }
        return ServiceNode.builder()
                .aiService(aiService)
                .clientId(client.getId())
                .clientName(client.getName())
                .clientDescription(client.getDescription())
                .clientStatus(client.getStatus())
                .systemPrompt(client.getSystemPrompt())
                .role(client.getRole())
                .build();
    }

    /**
     * 构建AiService实例
     *
     * @param client         Client实体
     * @param chatModel      StreamingChatModel实例
     * @param embeddingStore EmbeddingStore实例（可选）
     * @param embeddingModel EmbeddingModel实例（可选）
     * @return AiService实例
     */
    private AiService buildAiService(AgentEntity client, StreamingChatModel chatModel,
                                     EmbeddingStore embeddingStore, EmbeddingModel embeddingModel) {
        try {
            AiServices<AiService> builder = AiServices.builder(AiService.class)
                    .streamingChatModel(chatModel);
            if (embeddingStore != null && embeddingModel != null) {
                EmbeddingStoreContentRetriever build = EmbeddingStoreContentRetriever.builder().embeddingStore(embeddingStore)
                        .embeddingModel(embeddingModel)
                        .build();
                builder.contentRetriever(build);
            }
            if (client.getSystemPrompt() != null && !client.getSystemPrompt().isEmpty()) {
                builder.systemMessageProvider((e) -> client.getSystemPrompt());
            }
            return builder.build();
        } catch (Exception e) {
            log.error("多agent构建中 构建AiService失败: agentId={}, error={}", client.getId(), e.getMessage(), e);
            throw new RuntimeException("构建AiService失败: agentId=" + client.getId(), e);
        }
    }

    @Inject
    private OrchestratorNode orchestratorNode;

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return orchestratorNode;
    }
}
