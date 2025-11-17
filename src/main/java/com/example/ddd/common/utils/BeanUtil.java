package com.example.ddd.common.utils;

import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.domain.agent.service.armory.AiService;
import com.example.ddd.domain.agent.service.armory.ServiceNode;
import com.example.ddd.domain.agent.service.execute.Orchestrator;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

/**
 * Bean工具类
 * 提供统一的Bean注册方法
 */
@Slf4j
@Singleton
public class BeanUtil {
    @Inject
    private ApplicationContext applicationContext;

    private final Map<Long, List<ServiceNode>> agentServiceNodes = new ConcurrentHashMap<>();

    private final Map<Long, Orchestrator> agentOrchestrators = new ConcurrentHashMap<>();
    /**
     * 注册Bean（不带qualifier）
     *
     * @param bean Bean实例
     */

    /**
     * 注册Bean（带类型和qualifier）
     *
     * @param beanType      Bean类型
     * @param bean          Bean实例
     * @param qualifierName Qualifier名称
     */
    @SuppressWarnings("unchecked")
    public void registerBean(Class<?> beanType, Object bean, String qualifierName) {
        applicationContext.registerSingleton(
                (Class<Object>) beanType,
                bean,
                Qualifiers.byName(qualifierName)
        );

    }

    /**
     * 注册ChatModel
     *
     * @param modelId   模型ID
     * @param chatModel ChatModel实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerChatModel(Long modelId, StreamingChatModel chatModel) {
        String qualifier = "ChatModel" + COLON + modelId;
        try {
            StreamingChatModel existing = getBean(StreamingChatModel.class, qualifier);
            if (existing != null) {
                return true;
            }
        } catch (Exception e) {
            // Bean不存在是正常情况，忽略异常
        }
        registerBean(StreamingChatModel.class, chatModel, qualifier);
        return true;
    }

    /**
     * 注册EmbeddingStore
     *
     * @param ragId          RAG ID
     * @param embeddingStore EmbeddingStore实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerEmbeddingStore(Long ragId, EmbeddingStore<?> embeddingStore) {
        String qualifier = "EmbeddingStore" + COLON + ragId;

        EmbeddingStore<?> existing = getBean(EmbeddingStore.class, qualifier);
        if (existing != null) {
            return true;
        }
        registerBean(EmbeddingStore.class, embeddingStore, qualifier);
        return true;
    }

    /**
     * 注册EmbeddingModel
     *
     * @param ragId          RAG ID
     * @param embeddingModel EmbeddingModel实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerEmbeddingModel(Long ragId, EmbeddingModel embeddingModel) {
        String qualifier = "EmbeddingModel" + COLON + ragId;

        EmbeddingModel existing = getBean(EmbeddingModel.class, qualifier);
        if (existing != null) {
            return true;
        }

        registerBean(EmbeddingModel.class, embeddingModel, qualifier);
        return true;
    }


    /**
     * 根据qualifier名称获取Bean
     *
     * @param beanType      Bean类型
     * @param qualifierName Qualifier名称
     * @param <T>           Bean类型
     * @return Bean实例
     */
    public <T> T getBean(Class<T> beanType, String qualifierName) {
        try {
            return applicationContext.getBean(beanType, Qualifiers.byName(qualifierName));

        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 获取ChatModel
     *
     * @param modelId 模型ID
     * @return ChatModel实例
     */
    public StreamingChatModel getChatModel(Long modelId) {
        return getBean(StreamingChatModel.class, "ChatModel" + COLON + modelId);
    }

    /**
     * 获取EmbeddingStore
     *
     * @param ragId RAG ID
     * @return EmbeddingStore实例
     */
    public EmbeddingStore<?> getEmbeddingStore(Long ragId) {
        return getBean(EmbeddingStore.class, "EmbeddingStore" + COLON + ragId);
    }

    /**
     * 获取EmbeddingModel
     *
     * @param ragId RAG ID
     * @return EmbeddingModel实例
     */
    public EmbeddingModel getEmbeddingModel(Long ragId) {
        return getBean(EmbeddingModel.class, "EmbeddingModel" + COLON + ragId);
    }

    /**
     * 注册AiService
     *
     * @param clientId  Client ID
     * @param aiService AiService实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerAiService(Long clientId, AiService aiService) {
        String qualifier = "AiService" + COLON + clientId;

        AiService existing = getBean(AiService.class, qualifier);
        if (existing != null) {
            return true;
        }

        registerBean(AiService.class, aiService, qualifier);
        return true;
    }

    /**
     * 获取AiService
     *
     * @param clientId Client ID
     * @return AiService实例
     */
    public AiService getAiService(Long clientId) {
        return getBean(AiService.class, "AiService" + COLON + clientId);
    }

    /**
     * 注册ServiceNode到Agent
     *
     * @param agentId     Agent ID
     * @param serviceNode ServiceNode实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerServiceNode(Long agentId, ServiceNode serviceNode) {
        List<ServiceNode> nodes = agentServiceNodes.computeIfAbsent(agentId, k -> new ArrayList<>());
        // 检查是否已存在相同 clientId 的 ServiceNode
        boolean exists = nodes.stream()
                .anyMatch(node -> node.getClientId().equals(serviceNode.getClientId()));
        if (exists) {
            return true;
        }
        nodes.add(serviceNode);
        log.debug("注册ServiceNode: agentId={}, role={}, clientId={}",
                agentId, serviceNode.getRole(), serviceNode.getClientId());
        return true;
    }

    /**
     * 获取Agent的所有ServiceNode
     *
     * @param agentId Agent ID
     * @return ServiceNode列表
     */
    public List<ServiceNode> getServiceNodes(Long agentId) {
        return new ArrayList<>(agentServiceNodes.getOrDefault(agentId, new ArrayList<>()));
    }


    /**
     * 注册Orchestrator到Agent
     *
     * @param agentId      Agent ID
     * @param orchestrator Orchestrator实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerOrchestrator(Long agentId, Orchestrator orchestrator) {
        if (agentOrchestrators.containsKey(agentId)) {
            return true;
        }
        agentOrchestrators.put(agentId, orchestrator);
        log.debug("注册Orchestrator: agentId={}", agentId);
        return true;
    }

    /**
     * 获取Agent的Orchestrator
     *
     * @param agentId Agent ID
     * @return Orchestrator实例
     */
    public Orchestrator getOrchestrator(Long agentId) {
        return agentOrchestrators.get(agentId);
    }

    /**
     * 注册MCP
     *
     * @param mcpId    MCP ID
     * @param mcpEntity MCP实体
     * @return 是否注册成功
     */
    public boolean registerMcp(Long mcpId, McpEntity mcpEntity) {
        String qualifier = "Mcp" + COLON + mcpId;
        McpEntity existing = getBean(McpEntity.class, qualifier);
        if (existing != null) {
            return true;
        }
        registerBean(McpEntity.class, mcpEntity, qualifier);
        return true;
    }

    /**
     * 获取MCP
     *
     * @param mcpId MCP ID
     * @return MCP实体
     */
    public McpEntity getMcp(Long mcpId) {
        return getBean(McpEntity.class, "Mcp" + COLON + mcpId);
    }

    /**
     * 注册MCP Client
     *
     * @param mcpId    MCP ID
     * @param mcpClient MCP客户端实例
     * @return 是否注册成功
     */
    public boolean registerMcpClient(Long mcpId, McpClient mcpClient) {
        String qualifier = "McpClient" + COLON + mcpId;
       McpClient existing = getBean(McpClient.class, qualifier);
        if (existing != null) {
            return true;
        }
        registerBean(McpClient.class, mcpClient, qualifier);
        return true;
    }

    /**
     * 获取MCP Client
     *
     * @param mcpId MCP ID
     * @return MCP客户端实例
     */
    public dev.langchain4j.mcp.client.McpClient getMcpClient(Long mcpId) {
        return getBean(dev.langchain4j.mcp.client.McpClient.class, "McpClient" + COLON + mcpId);
    }

}
