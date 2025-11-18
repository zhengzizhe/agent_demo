package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.domain.agent.model.entity.*;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolProvider;
import dev.langchain4j.store.embedding.EmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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
        Map<Long, ChatModelEntity> modelMap = JSON.parseObject(chatModelJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, ChatModelEntity>>() {
                });
        if (modelMap == null || modelMap.isEmpty()) {
            log.warn("多agent构建中 orchestratorId={} 没有配置Model，跳过ServiceNode构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        String ragJson = dynamicContext.get(RAG_KEY);
        Map<Long, List<RagEntity>> ragMap = JSON.parseObject(ragJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<RagEntity>>>() {
                });
        String mcpJson = dynamicContext.get(MCP_KEY);
        Map<Long, List<McpEntity>> mcpMap = JSON.parseObject(mcpJson,
                new com.fasterxml.jackson.core.type.TypeReference<Map<Long, List<McpEntity>>>() {
                });
        String agentJson = dynamicContext.get(AGENT_KEY);
        OrchestratorEntity agent = JSON.parseObject(agentJson, OrchestratorEntity.class);
        try {
            build(agent, clients, modelMap, ragMap, mcpMap);
        } catch (Exception e) {
            log.error("多agent构建中 构建ServiceNode失败: orchestratorId={}, error={}", agentId, e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.debug("多agent构建中 ServiceNode构建完成: orchestratorId={}", agentId);
        return router(armoryCommandEntity, dynamicContext);
    }

    private void build(OrchestratorEntity agent, List<AgentEntity> clients, Map<Long, ChatModelEntity> modelMap, Map<Long, List<RagEntity>> ragMap, Map<Long, List<McpEntity>> mcpMap) {
        Long agentId = agent.getId();
        log.info("多agent构建中 开始构建AiService: orchestratorId={}, agent数量={}", agentId, clients.size());
        for (AgentEntity client : clients) {
            try {
                ChatModelEntity chatModelEntity = modelMap.get(client.getId());
                if (chatModelEntity == null) {
                    log.warn("多agent构建中 agentId={} 没有配置ChatModel，跳过AiService构建", client.getId());
                    continue;
                }
                StreamingChatModel chatModel = beanUtil.getChatModel(agentId, chatModelEntity.getId());
                if (chatModel == null) {
                    log.warn("多agent构建中 ChatModel {} 未找到，跳过AiService构建: orchestratorId={}, agentId={}",
                            chatModelEntity.getId(), agentId, client.getId());
                    continue;
                }
                List<RagEntity> rags = ragMap != null ? ragMap.get(client.getId()) : null;
                List<McpEntity> mcps = mcpMap != null ? mcpMap.get(client.getId()) : null;
                AiService aiService = buildAiService(agentId, client, chatModel, rags, mcps);
                beanUtil.registerAiService(agentId, client.getId(), aiService);
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
     * @param orchestratorId Orchestrator ID
     * @param client         Client实体
     * @param chatModel      StreamingChatModel实例（单个model）
     * @param rags           RAG列表（可选，支持多个RAG）
     * @param mcps           MCP列表（可选，支持多个MCP）
     * @return AiService实例
     */
    private AiService buildAiService(Long orchestratorId, AgentEntity client, StreamingChatModel chatModel,
                                     List<RagEntity> rags, List<McpEntity> mcps) {
        try {
            AiServices<AiService> builder = AiServices.builder(AiService.class)

                    .streamingChatModel(chatModel);
            if (rags != null && !rags.isEmpty()) {
                List<ContentRetriever> contentRetrievers = new ArrayList<>();
                for (RagEntity ragEntity : rags) {
                    EmbeddingStore<?> embeddingStore = beanUtil.getEmbeddingStore(orchestratorId, ragEntity.getId());
                    EmbeddingModel embeddingModel = beanUtil.getEmbeddingModel(orchestratorId, ragEntity.getId());
                    if (embeddingStore != null && embeddingModel != null) {
                        @SuppressWarnings("unchecked")
                        EmbeddingStore<TextSegment> textSegmentStore = (EmbeddingStore<TextSegment>) embeddingStore;
                        EmbeddingStoreContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                                .embeddingStore(textSegmentStore)
                                .embeddingModel(embeddingModel)
                                .build();
                        contentRetrievers.add(retriever);
                        log.debug("多agent构建中 收集RAG ContentRetriever: ragId={}, ragName={}, agentId={}",
                                ragEntity.getId(), ragEntity.getName(), client.getId());
                    } else {
                        if (embeddingStore == null) {
                            log.warn("多agent构建中 EmbeddingStore {} 未找到，跳过该RAG: agentId={}", ragEntity.getId(), client.getId());
                        }
                        if (embeddingModel == null) {
                            log.warn("多agent构建中 EmbeddingModel {} 未找到，跳过该RAG: agentId={}", ragEntity.getId(), client.getId());
                        }
                    }
                }

                if (!contentRetrievers.isEmpty()) {
                    ContentRetriever contentRetriever = contentRetrievers.get(0);
                    if (contentRetrievers.size() > 1) {
                        log.info("多agent构建中 agentId={} 配置了{}个RAG，使用第一个RAG",
                                client.getId(), contentRetrievers.size());
                    }
                    builder.contentRetriever(contentRetriever);
                }
            }

            // 支持多个MCP：遍历所有MCP并添加
            if (mcps != null && !mcps.isEmpty()) {
                List<McpClient> mcpClients = new ArrayList<>();
                for (McpEntity mcp : mcps) {
                    if ("ACTIVE".equals(mcp.getStatus())) {
                        McpClient mcpClient = beanUtil.getMcpClient(orchestratorId, mcp.getId());
                        if (mcpClient != null) {
                            mcpClients.add(mcpClient);
                            log.debug("多agent构建中 获取MCP Client: mcpId={}, mcpName={}", mcp.getId(), mcp.getName());
                        } else {
                            log.warn("多agent构建中 MCP Client未找到: mcpId={}, mcpName={}", mcp.getId(), mcp.getName());
                        }
                    }
                }
                if (!mcpClients.isEmpty()) {
                    ToolProvider toolProvider = createMcpToolProvider(mcpClients);
                    builder.toolProvider(toolProvider);
                    log.info("多agent构建中 为agentId={} 添加MCP ToolProvider: mcp数量={}", client.getId(), mcpClients.size());
                }
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

    /**
     * 创建MCP ToolProvider
     * 从MCP Client获取工具并返回
     *
     * @param mcpClients MCP客户端列表
     * @return ToolProvider实例
     */
    private ToolProvider createMcpToolProvider(List<McpClient> mcpClients) {
        return request -> McpToolProvider.builder().mcpClients(mcpClients).build().provideTools(request);
    }

    @Inject
    private OrchestratorNode orchestratorNode;

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return orchestratorNode;
    }
}
