package com.example.ddd.domain.agent.service;

import com.example.ddd.configuration.config.OpenAIProperties;
import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.adapter.repository.IVectorDocumentRepository;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
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

    @Inject
    private OpenAIProperties openAIProperties;

    /**
     * 添加文档到RAG知识库
     *
     * @param ragId    RAG ID
     * @param text     文档文本
     * @param metadata 文档元数据
     * @return 添加的文档块数量
     */
    public int addDocument(Long ragId, String text, Map<String, Object> metadata) {
        return dslContextFactory.callable(dslContext -> {
            RagEntity rag = ragRepository.queryById(dslContext, ragId);
            if (rag == null) {
                throw new IllegalArgumentException("RAG not found: " + ragId);
            }
            int chunkSize = rag.getChunkSize() != null ? rag.getChunkSize() : 1000;
            int chunkOverlap = rag.getChunkOverlap() != null ? rag.getChunkOverlap() : 200;
            Document document = Document.from(text);
            List<TextSegment> segments = DocumentSplitters.recursive(chunkSize, chunkOverlap).split(document);
            EmbeddingModel embeddingModel = createEmbeddingModel(rag);
            List<VectorDocumentEntity> vectorDocuments = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                Embedding embedding = embeddingModel.embed(segment).content();
                VectorDocumentEntity vectorDoc = new VectorDocumentEntity();
                vectorDoc.setRagId(ragId);
                vectorDoc.setContent(segment.text());
                vectorDoc.setEmbedding(embedding.vector());
                vectorDoc.setChunkIndex(i);
                Map<String, Object> chunkMetadata = new HashMap<>();
                if (segment.metadata() != null) {
                    if (metadata != null) {
                        chunkMetadata.putAll(metadata);
                    }
                } else if (metadata != null) {
                    chunkMetadata.putAll(metadata);
                }
                chunkMetadata.put("chunk_index", i);
                chunkMetadata.put("total_chunks", segments.size());
                vectorDoc.setMetadata(chunkMetadata);
                vectorDocuments.add(vectorDoc);
            }

            // 4. 批量存储向量文档
            if (!vectorDocuments.isEmpty()) {
                return vectorDocumentRepository.batchInsert(dslContext, vectorDocuments);
            }
            return 0;
        });
    }

    public List<VectorDocumentEntity> search(Long ragId, String queryText, int limit, double similarityThreshold) {
        return dslContextFactory.callable(dslContext -> {
            // 1. 获取RAG配置
            RagEntity rag = ragRepository.queryById(dslContext, ragId);
            if (rag == null) {
                throw new IllegalArgumentException("RAG not found: " + ragId);
            }
            // 2. 将查询文本向量化
            EmbeddingModel embeddingModel = createEmbeddingModel(rag);
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


    private EmbeddingModel createEmbeddingModel(RagEntity rag) {
        // 默认使用OpenAI的text-embedding-ada-002模型
        String embeddingModelName = rag.getEmbeddingModel() != null
                ? rag.getEmbeddingModel()
                : "text-embedding-ada-002";
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAIProperties.getKey())
                .baseUrl(openAIProperties.getUrl())
                .modelName(embeddingModelName)
                .build();
    }
}

