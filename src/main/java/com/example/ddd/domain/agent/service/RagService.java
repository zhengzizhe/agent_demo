package com.example.ddd.domain.agent.service;

import com.example.ddd.configuration.config.OpenAIProperties;
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
        RagEntity rag = dslContextFactory.callable(dslContext -> {
            return ragRepository.queryById(dslContext, ragId);
        });
        if (rag == null) {
            throw new IllegalArgumentException("未找到RAG: " + ragId);
        }
        EmbeddingModel embeddingModel = createEmbeddingModel();
        var splitter = DocumentSplitters.recursive(1000, 200);
        Document document = Document.from(text, Metadata.from(metadata != null ? metadata : new HashMap<>()));
        List<TextSegment> segments = splitter.split(document);
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        List<VectorDocumentEntity> entities = new java.util.ArrayList<>();
        long currentTime = System.currentTimeMillis() / 1000; // 秒级时间戳
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            Embedding embedding = embeddings.get(i);
            
            VectorDocumentEntity entity = new VectorDocumentEntity();
            entity.setRagId(ragId);
            entity.setContent(segment.text());
            entity.setEmbedding(embedding.vector());
            entity.setChunkIndex(i);
            entity.setCreatedAt(currentTime);
            entity.setUpdatedAt(currentTime);
            
            // 合并metadata
            Map<String, Object> mergedMetadata = new HashMap<>();
            if (metadata != null) {
                mergedMetadata.putAll(metadata);
            }
            // 添加chunk索引到metadata
            mergedMetadata.put("chunkIndex", i);
            mergedMetadata.put("totalChunks", segments.size());
            entity.setMetadata(mergedMetadata);
            
            entities.add(entity);
        }
        
        // 批量插入到数据库
        int insertedCount = dslContextFactory.callable(dslContext -> {
            return vectorDocumentRepository.batchInsert(dslContext, entities);
        });
        
        log.info("文档已添加到RAG: ragId={}, segments={}, insertedCount={}", ragId, segments.size(), insertedCount);
        return insertedCount;
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
     * 根据RAG ID查询所有文档
     *
     * @param ragId RAG ID
     * @return 文档列表
     */
    public List<VectorDocumentEntity> queryDocuments(Long ragId) {
        return dslContextFactory.callable(dslContext -> {
            return vectorDocumentRepository.queryByRagId(dslContext, ragId);
        });
    }

    /**
     * 根据ID删除文档
     *
     * @param docId 文档ID
     * @return 删除的文档数量
     */
    public int deleteDocument(Long docId) {
        return dslContextFactory.callable(dslContext -> {
            return vectorDocumentRepository.deleteById(dslContext, docId);
        });
    }


    /**
     * 创建EmbeddingModel（硬编码配置）
     */
    private EmbeddingModel createEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(openAIProperties.getKey())
                .baseUrl(openAIProperties.getUrl())
                .modelName("text-embedding-3-small")
                .build();
    }

}

