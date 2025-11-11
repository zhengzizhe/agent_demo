package com.example.ddd.domain.agent.service;

import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.adapter.repository.IVectorDocumentRepository;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务
 * 提供文档分块、向量化、存储和检索功能
 */
@Singleton
@Slf4j
public class RagService {

    @Inject
    private IRagRepository ragRepository;

    @Inject
    private IVectorDocumentRepository vectorDocumentRepository;

    @Inject
    private DSLContextFactory dslContextFactory;


    /**
     * 添加文档到RAG知识库
     *
     * @param ragId    RAG ID（保留参数，但使用硬编码配置）
     * @param text     文档文本
     * @param metadata 文档元数据
     * @return 添加的文档块数量
     */
    public int addDocument(Long ragId, String text, Map<String, Object> metadata) {
        EmbeddingStore<TextSegment> store = createEmbeddingStore();
        EmbeddingModel embeddingModel = createEmbeddingModel();
        var splitter = DocumentSplitters.recursive(1000, 200);
        Document document = Document.from(text, Metadata.from(metadata != null ? metadata : new HashMap<>()));
        List<TextSegment> segments = splitter.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        List<String> embeddingStoreIds = store.addAll(embeddings, segments);
        log.info("文档已添加到RAG: ragId={}, segments={}, embeddingStoreIds={}", ragId, segments.size(), embeddingStoreIds.size());
        return segments.size();
            }
    public List<VectorDocumentEntity> search(Long ragId, String queryText, int limit, double similarityThreshold) {
        return dslContextFactory.callable(dslContext -> {
            RagEntity rag = ragRepository.queryById(dslContext, ragId);
            if (rag == null) {
                throw new IllegalArgumentException("未找到RAG: " + ragId);
            }
            EmbeddingModel embeddingModel = createEmbeddingModel();
            TextSegment querySegment = TextSegment.from(queryText);
            Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
            return vectorDocumentRepository.similaritySearch(
                    dslContext,
                    ragId,
                    queryEmbedding.vector(),
                    limit,
                    similarityThreshold
            );
        });
    }

    public int deleteAllDocuments(Long ragId) {
        return dslContextFactory.callable(dslContext -> {
            return vectorDocumentRepository.deleteByRagId(dslContext, ragId);
        });
    }


    /**
     * 创建EmbeddingModel（硬编码配置）
     */
    private EmbeddingModel createEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey("sk-ReRvqCmAoA71cgoiC416F0809fEb46A28f7a07AcDd3e339e")
                .baseUrl("https://apis.itedus.cn/v1/")
                .modelName("text-embedding-3-small")
                .build();
    }

    /**
     * 创建PgVector向量存储（硬编码配置）
     */
    private PgVectorEmbeddingStore createEmbeddingStore() {
        return PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .user("postgres")
                .password("postgres")
                .database("agent")
                .table("vector_document")
                .dimension(1536)
                .useIndex(true)
                .indexListSize(1000)
                .createTable(true)
                .build();
    }
}

