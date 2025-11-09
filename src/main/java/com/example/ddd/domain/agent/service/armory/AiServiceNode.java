package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.configuration.config.ChatApi;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.model.entity.ChatModelEntity;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
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
        for (ClientEntity client : clients) {
            Long clientId = client.getId();
            List<ChatModelEntity> chatModels = modelMap.get(clientId);
            if (chatModels == null || chatModels.isEmpty()) {
                log.warn("Client {} 没有配置Model，跳过ChatApi构建", clientId);
                continue;
            }
            for (ChatModelEntity chatModelEntity : chatModels) {
                Long modelId = chatModelEntity.getId();
                log.info("Ai agent构建中 构建ChatApi: clientId={}, modelId={}, modelName={}",
                        clientId, modelId, chatModelEntity.getName());
                try {
                    StreamingChatModel chatModel = beanUtil.getChatModel(modelId);
                    if (chatModel == null) {
                        log.warn("ChatModel {} 未找到，跳过ChatApi构建", modelId);
                        continue;
                    }
                    ChatApi chatApi = createChatApi(client, chatModel, ragMap != null ? ragMap.get(clientId) : null);
                    beanUtil.registerChatApi(clientId, chatApi);
                    log.info("ChatApi构建完成: clientId={}, modelId={}", clientId, modelId);
                } catch (Exception e) {
                    log.error("构建ChatApi失败: clientId={}, modelId={}, error={}",
                            clientId, modelId, e.getMessage(), e);
                }
            }
        }

        log.info("Ai agent构建完成 agentId:{}", agentId);
        return router(armoryCommandEntity, dynamicContext);
    }

    /**
     * 创建 ChatApi 实例
     * 如果配置了 RAG，则使用 RAG 增强的 ChatApi
     */
    private ChatApi createChatApi(ClientEntity client, StreamingChatModel chatModel, List<RagEntity> rags) {
        AiServices<ChatApi> chatApiAiServices = AiServices.builder(ChatApi.class)
                .streamingChatModel(chatModel)
                .systemMessageProvider((t) -> client.getSystemPrompt());
        for (RagEntity rag : rags) {
            EmbeddingStore<TextSegment> embeddingStore =
                    (EmbeddingStore<TextSegment>) beanUtil.getEmbeddingStore(rag.getId());
            if (embeddingStore != null) {
                ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                        .embeddingStore(embeddingStore)
                        .embeddingModel(beanUtil.getEmbeddingModel(rag.getId()))
                        .maxResults(5) // 默认返回5个最相关的结果
                        .minScore(0.6) // 最小相似度分数
                        .build();
                chatApiAiServices
                        .contentRetriever(contentRetriever);

            }
        }
        return chatApiAiServices.build();
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return null;
    }
}
