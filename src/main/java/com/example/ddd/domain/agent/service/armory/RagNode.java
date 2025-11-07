package com.example.ddd.domain.agent.service.armory;

import com.example.ddd.common.utils.BeanUtil;
import com.example.ddd.common.utils.ILogicHandler;
import com.example.ddd.common.utils.JSON;
import com.example.ddd.configuration.config.OpenAIProperties;
import com.example.ddd.domain.agent.model.entity.ArmoryCommandEntity;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import static com.example.ddd.common.constant.IAgentConstant.RAG_KEY;

@Slf4j
@Singleton
public class RagNode extends AbstractArmorySupport {
    @Inject
    ChatModelNode chatModelNode;

    @Inject
    BeanUtil beanUtil;

    @Inject
    OpenAIProperties openAIProperties;

    @Override
    public String handle(ArmoryCommandEntity armoryCommandEntity, DynamicContext dynamicContext) {
        Long agentId = armoryCommandEntity.getAgentId();
        String ragJson = dynamicContext.get(RAG_KEY);
        Map<Long, List<RagEntity>> ragMap = JSON.parseObject(ragJson,
                new com.fasterxml.jackson.core.type.TypeReference<>() {
                });
        if (ragMap == null || ragMap.isEmpty()) {
            log.warn("Agent {} 没有配置RAG，跳过RAG构建", agentId);
            return router(armoryCommandEntity, dynamicContext);
        }
        ragMap.values().stream()
                .flatMap(List::stream)
                .forEach(ragEntity -> {
                    log.info("Ai agent构建中 构建rag:{} agentId:{}", ragEntity.getName(), agentId);
                    PgVectorEmbeddingStore embeddingStore = getEmbeddingStore(ragEntity);
                    beanUtil.registerEmbeddingStore(ragEntity.getId(), embeddingStore);
                    EmbeddingModel embeddingModel = createEmbeddingModel(ragEntity);
                    beanUtil.registerEmbeddingModel(ragEntity.getId(), embeddingModel);
                });

        return router(armoryCommandEntity, dynamicContext);
    }

    /**
     * 创建EmbeddingModel
     */
    private EmbeddingModel createEmbeddingModel(RagEntity ragEntity) {
        // 默认使用OpenAI的text-embedding-ada-002模型
        String embeddingModelName = ragEntity.getEmbeddingModel() != null
                ? ragEntity.getEmbeddingModel()
                : "text-embedding-ada-002";
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAIProperties.getKey())
                .baseUrl(openAIProperties.getUrl())
                .modelName(embeddingModelName)
                .build();
    }

    /**
     * 创建PgVector向量存储
     */
    public PgVectorEmbeddingStore getEmbeddingStore(RagEntity ragEntity) {
        String host = ragEntity.getDatabaseHost() != null ? ragEntity.getDatabaseHost() : "localhost";
        Integer port = ragEntity.getDatabasePort() != null ? ragEntity.getDatabasePort() : 5432;
        String database = ragEntity.getDatabaseName() != null ? ragEntity.getDatabaseName() : "postgres";
        String user = ragEntity.getDatabaseUser() != null ? ragEntity.getDatabaseUser() : "postgres";
        String password = ragEntity.getDatabasePassword() != null ? ragEntity.getDatabasePassword() : "postgres";
        String table = ragEntity.getTableName() != null ? ragEntity.getTableName() : "vector_document";
        Integer dimension = ragEntity.getDimension() != null ? ragEntity.getDimension() : 1536;
        Boolean useIndex = ragEntity.getUseIndex() != null ? ragEntity.getUseIndex() : true;
        Integer indexListSize = ragEntity.getIndexListSize() != null ? ragEntity.getIndexListSize() : 1000;
        PgVectorEmbeddingStore.PgVectorEmbeddingStoreBuilder builder = PgVectorEmbeddingStore.builder()
                .host(host)
                .port(port)
                .user(user)
                .password(password)
                .database(database)
                .table(table)
                .dimension(dimension)
                .useIndex(useIndex)
                .indexListSize(indexListSize)
                .createTable(true);
        return builder.build();
    }

    @Override
    public ILogicHandler<ArmoryCommandEntity, DynamicContext, String> getNextHandler() {
        return chatModelNode;
    }
}
