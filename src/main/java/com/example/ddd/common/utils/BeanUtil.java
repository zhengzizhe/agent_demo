package com.example.ddd.common.utils;

import com.example.ddd.configuration.config.ChatApi;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import io.micronaut.context.ApplicationContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

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

    /**
     * 注册Bean（不带qualifier）
     *
     * @param bean Bean实例
     */
    public void registerBean(Object bean) {
        applicationContext.registerSingleton(bean);
    }

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
     * @return 是否为新注册（true=新注册，false=复用已存在的）
     */
    public boolean registerChatModel(Long modelId, ChatModel chatModel) {
        String qualifier = "ChatModel" + COLON + modelId;
        try {
            ChatModel existing = getBean(ChatModel.class, qualifier);
            if (existing != null) {
                return false;
            }
        } catch (Exception e) {
        }
        registerBean(ChatModel.class, chatModel, qualifier);
        return true;
    }

    /**
     * 注册EmbeddingStore
     *
     * @param ragId          RAG ID
     * @param embeddingStore EmbeddingStore实例
     * @return 是否为新注册（true=新注册，false=更新已存在的）
     */
    public boolean registerEmbeddingStore(Long ragId, EmbeddingStore<?> embeddingStore) {
        String qualifier = "EmbeddingStore" + COLON + ragId;
        boolean existed = false;
        try {
            EmbeddingStore<?> existing = getBean(EmbeddingStore.class, qualifier);
            if (existing != null) {
                existed = true;
                log.warn("更新已存在的EmbeddingStore: ragId={}", ragId);
            }
        } catch (Exception e) {
        }
        registerBean(EmbeddingStore.class, embeddingStore, qualifier);
        return !existed;
    }

    /**
     * 注册EmbeddingModel
     *
     * @param ragId          RAG ID
     * @param embeddingModel EmbeddingModel实例
     * @return 是否为新注册（true=新注册，false=更新已存在的）
     */
    public boolean registerEmbeddingModel(Long ragId, EmbeddingModel embeddingModel) {
        String qualifier = "EmbeddingModel" + COLON + ragId;
        boolean existed = false;
        try {
            EmbeddingModel existing = getBean(EmbeddingModel.class, qualifier);
            if (existing != null) {
                existed = true;
                log.warn("更新已存在的EmbeddingModel: ragId={}", ragId);
            }
        } catch (Exception e) {
        }
        registerBean(EmbeddingModel.class, embeddingModel, qualifier);
        return !existed;
    }

    /**
     * 注册ChatApi
     *
     * @param clientId apiId
     * @param chatApi  ChatApi实例
     */
    public void registerChatApi(Long clientId, ChatApi chatApi) {
        String qualifier = "ChatApi" + COLON + clientId;
        registerBean(ChatApi.class, chatApi, qualifier);
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
        return applicationContext.getBean(beanType, Qualifiers.byName(qualifierName));
    }

    /**
     * 获取ChatModel
     *
     * @param modelId 模型ID
     * @return ChatModel实例
     */
    public ChatModel getChatModel(Long modelId) {
        return getBean(ChatModel.class, "ChatModel" + COLON + modelId);
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
}
