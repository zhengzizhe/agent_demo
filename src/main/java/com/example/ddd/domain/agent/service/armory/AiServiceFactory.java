package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.domain.agent.model.entity.ClientEntity;
import com.example.ddd.domain.agent.service.execute.CriticService.CriticService;
import com.example.ddd.domain.agent.service.execute.ExecutorService.ExecutorService;
import com.example.ddd.domain.agent.service.execute.ResearcherService.ResearcherService;
import com.example.ddd.domain.agent.service.execute.SupervisorService.SupervisorService;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * AI服务工厂
 * 负责构建四个@AIService：Supervisor/Researcher/Executor/Critic
 */
@Slf4j
public class AiServiceFactory {

    private final BeanUtil beanUtil;

    public AiServiceFactory(BeanUtil beanUtil) {
        this.beanUtil = beanUtil;
    }

    /**
     * 构建SupervisorService
     * 仅文本，不绑定危险工具
     * 注意：方法返回TokenStream（流式），所以使用streamingChatModel
     */
    public SupervisorService buildSupervisor(StreamingChatModel streamingModel, ClientEntity clientEntity) {
        if (streamingModel == null) {
            throw new IllegalArgumentException("SupervisorService: chatModel cannot be null");
        }
        if (clientEntity == null) {
            throw new IllegalArgumentException("SupervisorService: clientEntity cannot be null");
        }
        
        String systemPrompt = clientEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException(
                String.format("Supervisor Client (id=%d, name=%s) 的 systemPrompt 不能为空，请从数据库配置", 
                    clientEntity.getId(), clientEntity.getName())
            );
        }
        
        final String finalSystemPrompt = systemPrompt;
        
        return AiServices.builder(SupervisorService.class)
                .streamingChatModel(streamingModel)  // 使用streamingChatModel（因为方法返回TokenStream）
                .systemMessageProvider(t -> finalSystemPrompt)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(3))  // 优化：从10减少到3，降低token消耗
                .build();
    }

    /**
     * 构建ResearcherService
     * 绑定RAG检索工具
     * 注意：方法返回TokenStream（流式），所以使用streamingChatModel
     */
    public ResearcherService buildResearcher(StreamingChatModel streamingModel, ClientEntity clientEntity, List<Long> ragIds) {
        if (streamingModel == null) {
            throw new IllegalArgumentException("ResearcherService: chatModel cannot be null");
        }
        if (clientEntity == null) {
            throw new IllegalArgumentException("ResearcherService: clientEntity cannot be null");
        }
        
        String systemPrompt = clientEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException(
                String.format("Researcher Client (id=%d, name=%s) 的 systemPrompt 不能为空，请从数据库配置", 
                    clientEntity.getId(), clientEntity.getName())
            );
        }
        
        final String finalSystemPrompt = systemPrompt;
        
        var builder = AiServices.builder(ResearcherService.class)
                .streamingChatModel(streamingModel)  // 使用streamingChatModel
                .systemMessageProvider(t -> finalSystemPrompt)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(2));  // 优化：Researcher减少到2条，专注当前任务
        if (ragIds != null && !ragIds.isEmpty()) {
            for (Long ragId : ragIds) {
                try {
                    @SuppressWarnings("unchecked")
                    EmbeddingStore<TextSegment> store = (EmbeddingStore<TextSegment>) beanUtil.getEmbeddingStore(ragId);
                    if (store != null) {
                        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                                .embeddingStore(store)
                                .embeddingModel(beanUtil.getEmbeddingModel(ragId))
                                .maxResults(1)  // 优化：从2减少到1，进一步降低token消耗（简单查询只需1条）
                                .minScore(0.7)  // 优化：提高相似度阈值，确保质量
                                .build();
                        builder.contentRetriever(contentRetriever);
                        log.info("为Researcher绑定RAG: ragId={}", ragId);
                    }
                } catch (Exception e) {
                    log.warn("绑定RAG失败: ragId={}, error={}", ragId, e.getMessage());
                }
            }
        }

        return builder.build();
    }

    public ExecutorService buildExecutor(StreamingChatModel streamingModel, ClientEntity clientEntity) {
        if (streamingModel == null) {
            throw new IllegalArgumentException("ExecutorService: chatModel cannot be null");
        }
        if (clientEntity == null) {
            throw new IllegalArgumentException("ExecutorService: clientEntity cannot be null");
        }
        
        String systemPrompt = clientEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException(
                String.format("Executor Client (id=%d, name=%s) 的 systemPrompt 不能为空，请从数据库配置", 
                    clientEntity.getId(), clientEntity.getName())
            );
        }
        final String finalSystemPrompt = systemPrompt;
        return AiServices.builder(ExecutorService.class)
                .streamingChatModel(streamingModel)  // 使用streamingChatModel
                .systemMessageProvider(t -> finalSystemPrompt)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(2))  // 优化：Executor减少到2条，避免历史干扰
                .build();
    }

    public CriticService buildCritic(StreamingChatModel streamingModel, ClientEntity clientEntity) {
        if (streamingModel == null) {
            throw new IllegalArgumentException("CriticService: chatModel cannot be null");
        }
        if (clientEntity == null) {
            throw new IllegalArgumentException("CriticService: clientEntity cannot be null");
        }
        String systemPrompt = clientEntity.getSystemPrompt();
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException(
                String.format("Critic Client (id=%d, name=%s) 的 systemPrompt 不能为空，请从数据库配置", 
                    clientEntity.getId(), clientEntity.getName())
            );
        }
        
        final String finalSystemPrompt = systemPrompt;
        
        return AiServices.builder(CriticService.class)
                .streamingChatModel(streamingModel)  // 使用streamingChatModel
                .systemMessageProvider(t -> finalSystemPrompt)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(1))  // 优化：Critic只需要1条，专注评审
                .build();
    }
}

