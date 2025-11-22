package com.example.ddd.common.utils;

import com.example.ddd.application.agent.service.Orchestrator;
import com.example.ddd.domain.agent.model.entity.McpEntity;
import com.example.ddd.application.agent.service.armory.AiService;
import com.example.ddd.application.agent.service.armory.ServiceNode;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.ddd.common.constant.IAgentConstant.COLON;

/**
 * Bean工具类
 * 提供统一的Bean注册方法，使用Spring容器动态注册Bean
 */
@Slf4j
@Component
public class BeanUtil {
    
    private final ApplicationContext applicationContext;
    
    public BeanUtil(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    /**
     * 注册Bean到Spring容器
     *
     * @param beanType      Bean类型
     * @param bean          Bean实例
     * @param qualifierName Bean名称（作为qualifier）
     */
    public void registerBean(Class<?> beanType, Object bean, String qualifierName) {
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            log.error("ApplicationContext is not ConfigurableApplicationContext, cannot register bean: {}", qualifierName);
            return;
        }
        
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        ConfigurableListableBeanFactory beanFactory = configurableContext.getBeanFactory();
        
        // 检查Bean是否已存在
        if (beanFactory.containsBean(qualifierName)) {
            log.debug("Bean already exists: {}", qualifierName);
            return;
        }
        
        // 创建Bean定义
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanType);
        beanDefinition.setInstanceSupplier(() -> bean);
        beanDefinition.setScope("singleton");
        
        // 注册Bean
        if (beanFactory instanceof BeanDefinitionRegistry) {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            registry.registerBeanDefinition(qualifierName, beanDefinition);
            log.debug("Registered bean: {} as {}", beanType.getSimpleName(), qualifierName);
        } else {
            log.error("BeanFactory is not BeanDefinitionRegistry, cannot register bean: {}", qualifierName);
        }
    }

    /**
     * 注册ChatModel（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param modelId        模型ID
     * @param chatModel      ChatModel实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerChatModel(String orchestratorId, String modelId, StreamingChatModel chatModel) {
        String qualifier = "Orchestrator" + COLON + orchestratorId + COLON + "ChatModel" + COLON + modelId;
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
     * 注册EmbeddingStore（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param ragId          RAG ID
     * @param embeddingStore EmbeddingStore实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerEmbeddingStore(String orchestratorId, String ragId, EmbeddingStore<?> embeddingStore) {
        String qualifier = "Orchestrator" + COLON + orchestratorId + COLON + "EmbeddingStore" + COLON + ragId;

        EmbeddingStore<?> existing = getBean(EmbeddingStore.class, qualifier);
        if (existing != null) {
            return true;
        }
        registerBean(EmbeddingStore.class, embeddingStore, qualifier);
        return true;
    }

    /**
     * 注册EmbeddingModel（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param ragId          RAG ID
     * @param embeddingModel EmbeddingModel实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerEmbeddingModel(String orchestratorId, String ragId, EmbeddingModel embeddingModel) {
        String qualifier = "Orchestrator" + COLON + orchestratorId + COLON + "EmbeddingModel" + COLON + ragId;

        EmbeddingModel existing = getBean(EmbeddingModel.class, qualifier);
        if (existing != null) {
            return true;
        }

        registerBean(EmbeddingModel.class, embeddingModel, qualifier);
        return true;
    }


    /**
     * 根据qualifier名称获取Bean
     * 注意：Spring Boot 中通过名称获取 Bean 使用 getBean(String name)
     * 这里使用 qualifierName 作为 bean 名称
     *
     * @param beanType      Bean类型
     * @param qualifierName Qualifier名称
     * @param <T>           Bean类型
     * @return Bean实例
     */
    public <T> T getBean(Class<T> beanType, String qualifierName) {
        try {
            // Spring Boot 中通过名称获取 Bean
            return applicationContext.getBean(qualifierName, beanType);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取ChatModel（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param modelId        模型ID
     * @return ChatModel实例
     */
    public StreamingChatModel getChatModel(String orchestratorId, String modelId) {
        return getBean(StreamingChatModel.class, "Orchestrator" + COLON + orchestratorId + COLON + "ChatModel" + COLON + modelId);
    }

    /**
     * 获取EmbeddingStore（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param ragId          RAG ID
     * @return EmbeddingStore实例
     */
    public EmbeddingStore<?> getEmbeddingStore(String orchestratorId, String ragId) {
        return getBean(EmbeddingStore.class, "Orchestrator" + COLON + orchestratorId + COLON + "EmbeddingStore" + COLON + ragId);
    }

    /**
     * 获取EmbeddingModel（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param ragId          RAG ID
     * @return EmbeddingModel实例
     */
    public EmbeddingModel getEmbeddingModel(String orchestratorId, String ragId) {
        return getBean(EmbeddingModel.class, "Orchestrator" + COLON + orchestratorId + COLON + "EmbeddingModel" + COLON + ragId);
    }

    /**
     * 注册AiService（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param clientId       Client ID
     * @param aiService      AiService实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerAiService(String orchestratorId, String clientId, AiService aiService) {
        String qualifier = "Orchestrator" + COLON + orchestratorId + COLON + "AiService" + COLON + clientId;

        AiService existing = getBean(AiService.class, qualifier);
        if (existing != null) {
            return true;
        }

        registerBean(AiService.class, aiService, qualifier);
        return true;
    }

    /**
     * 获取AiService（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param clientId       Client ID
     * @return AiService实例
     */
    public AiService getAiService(String orchestratorId, String clientId) {
        return getBean(AiService.class, "Orchestrator" + COLON + orchestratorId + COLON + "AiService" + COLON + clientId);
    }

    /**
     * 注册ServiceNode到Agent（注册到Spring容器）
     *
     * @param agentId     Agent ID
     * @param serviceNode ServiceNode实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerServiceNode(String agentId, ServiceNode serviceNode) {
        String qualifier = "Agent" + COLON + agentId + COLON + "ServiceNode" + COLON + serviceNode.getClientId();
        
        // 检查是否已存在
        ServiceNode existing = getBean(ServiceNode.class, qualifier);
        if (existing != null) {
            return true;
        }
        
        // 注册到Spring容器
        registerBean(ServiceNode.class, serviceNode, qualifier);
        log.debug("注册ServiceNode: agentId={}, role={}, clientId={}",
                agentId, serviceNode.getRole(), serviceNode.getClientId());
        return true;
    }

    /**
     * 获取Agent的所有ServiceNode（从Spring容器获取）
     *
     * @param agentId Agent ID
     * @return ServiceNode列表
     */
    public List<ServiceNode> getServiceNodes(String agentId) {
        String prefix = "Agent" + COLON + agentId + COLON + "ServiceNode" + COLON;
        
        if (!(applicationContext instanceof ConfigurableApplicationContext)) {
            return new ArrayList<>();
        }
        
        ConfigurableApplicationContext configurableContext = (ConfigurableApplicationContext) applicationContext;
        ConfigurableListableBeanFactory beanFactory = configurableContext.getBeanFactory();
        
        // 获取所有以prefix开头的Bean名称
        String[] beanNames = beanFactory.getBeanNamesForType(ServiceNode.class);
        List<ServiceNode> nodes = Arrays.stream(beanNames)
                .filter(name -> name.startsWith(prefix))
                .map(name -> getBean(ServiceNode.class, name))
                .filter(node -> node != null)
                .collect(Collectors.toList());
        
        return nodes;
    }


    /**
     * 注册Orchestrator到Agent（注册到Spring容器）
     *
     * @param agentId      Agent ID
     * @param orchestrator Orchestrator实例
     * @return 是否注册成功（如果已存在则返回true不处理，不存在则注册后返回true）
     */
    public boolean registerOrchestrator(String agentId, Orchestrator orchestrator) {
        String qualifier = "Agent" + COLON + agentId + COLON + "Orchestrator";
        
        // 检查是否已存在
        Orchestrator existing = getBean(Orchestrator.class, qualifier);
        if (existing != null) {
            return true;
        }
        
        // 注册到Spring容器
        registerBean(Orchestrator.class, orchestrator, qualifier);
        log.debug("注册Orchestrator: agentId={}", agentId);
        return true;
    }

    /**
     * 获取Agent的Orchestrator（从Spring容器获取）
     *
     * @param agentId Agent ID
     * @return Orchestrator实例
     */
    public Orchestrator getOrchestrator(String agentId) {
        String qualifier = "Agent" + COLON + agentId + COLON + "Orchestrator";
        return getBean(Orchestrator.class, qualifier);
    }

    /**
     * 注册MCP
     *
     * @param mcpId     MCP ID
     * @param mcpEntity MCP实体
     * @return 是否注册成功
     */
    public boolean registerMcp(String mcpId, McpEntity mcpEntity) {
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
    public McpEntity getMcp(String mcpId) {
        return getBean(McpEntity.class, "Mcp" + COLON + mcpId);
    }

    /**
     * 注册MCP Client（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param mcpId          MCP ID
     * @param mcpClient      MCP客户端实例
     * @return 是否注册成功
     */
    public boolean registerMcpClient(String orchestratorId, String mcpId, McpClient mcpClient) {
        String qualifier = "Orchestrator" + COLON + orchestratorId + COLON + "McpClient" + COLON + mcpId;
        McpClient existing = getBean(McpClient.class, qualifier);
        if (existing != null) {
            return true;
        }
        registerBean(McpClient.class, mcpClient, qualifier);
        return true;
    }

    /**
     * 获取MCP Client（Orchestrator 独有命名）
     *
     * @param orchestratorId Orchestrator ID
     * @param mcpId          MCP ID
     * @return MCP客户端实例
     */
    public dev.langchain4j.mcp.client.McpClient getMcpClient(String orchestratorId, String mcpId) {
        return getBean(dev.langchain4j.mcp.client.McpClient.class, "Orchestrator" + COLON + orchestratorId + COLON + "McpClient" + COLON + mcpId);
    }

}
