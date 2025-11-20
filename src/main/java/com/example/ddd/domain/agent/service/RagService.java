package com.example.ddd.domain.agent.service;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.configuration.config.OpenAIProperties;
import com.example.ddd.domain.agent.adapter.repository.IRagRepository;
import com.example.ddd.domain.agent.adapter.repository.IVectorDocumentRepository;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.domain.agent.model.valobj.EntityDTO;
import com.example.ddd.domain.agent.model.valobj.ExtractionResult;
import com.example.ddd.domain.agent.model.valobj.RelationDTO;
import com.example.ddd.infrastructure.adapter.repository.DocKgEntityRepository;
import com.example.ddd.infrastructure.adapter.repository.DocRepository;
import com.example.ddd.infrastructure.adapter.repository.KgEntityRepository;
import com.example.ddd.infrastructure.adapter.repository.KgRelationRepository;
import com.example.ddd.infrastructure.adapter.repository.RagDocRepository;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.trigger.response.DocView;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务
 * 提供文档向量化、存储和检索功能
 */
@Singleton
@Slf4j
public class RagService {

    @Inject
    private IRagRepository ragRepository;
    @Inject
    private DocRepository docRepository;

    @Inject
    private IVectorDocumentRepository vectorDocumentRepository;

    @Inject
    private DSLContextFactory dslContextFactory;

    @Inject
    private OpenAIProperties openAIProperties;
    @Inject
    private KgEntityRepository kgEntityRepository;
    @Inject
    private KgRelationRepository kgRelationRepository;
    @Inject
    private DocKgEntityRepository docKgEntityRepository;
    @Inject
    private RagDocRepository ragDocRepository;
    @Inject
    private IdGenerator idGenerator;

    /**
     * 添加文档到RAG知识库
     *
     * @param ragId    RAG ID
     * @param text     文档文本
     * @param metadata 文档元数据
     * @return 添加的文档数量（始终为1）
     */
    public int addDocument(Long ragId, String text, Map<String, Object> metadata) {
        RagEntity rag = dslContextFactory.callable(dslContext -> {
            return ragRepository.queryById(dslContext, ragId);
        });
        if (rag == null) {
            throw new IllegalArgumentException("未找到RAG: " + ragId);
        }
        EmbeddingModel embeddingModel = createEmbeddingModel();
        TextSegment documentSegment = TextSegment.from(text);
        Embedding embedding = embeddingModel.embed(documentSegment).content();
        VectorDocumentEntity entity = new VectorDocumentEntity();
        entity.setRagId(ragId);
        entity.setText(text);
        entity.setEmbedding(embedding.vector());
        entity.setChunkIndex(null); // 不再使用chunkIndex
        long currentTime = System.currentTimeMillis() / 1000; // 秒级时间戳
        entity.setCreatedAt(currentTime);
        entity.setUpdatedAt(currentTime);
        Map<String, Object> finalMetadata = new HashMap<>();
        if (metadata != null) {
            finalMetadata.putAll(metadata);
        }
        entity.setMetadata(finalMetadata);
        int insertedCount = dslContextFactory.callable(dslContext -> {
            vectorDocumentRepository.insert(dslContext, entity);
            return 1; // 始终返回1，表示添加了1个文档
        });
        log.info("文档已添加到RAG: ragId={}, insertedCount={}", ragId, insertedCount);
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

    /**
     * KG知识图谱搜索：先向量匹配，再Neo4j查询关系
     * 
     * @param queryText 查询文本
     * @param limit 向量匹配返回数量
     * @param similarityThreshold 相似度阈值
     * @param maxDepth Neo4j查询深度
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> searchKnowledgeGraph(String queryText, int limit, double similarityThreshold, int maxDepth) {
        return dslContextFactory.callable(dslContext -> {
            // 1. 向量匹配：将查询文本转换为向量，在kg_entity表中搜索相似实体
            EmbeddingModel embeddingModel = createEmbeddingModel();
            TextSegment querySegment = TextSegment.from(queryText);
            Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
            
            List<KgEntityPO> matchedEntities = kgEntityRepository.similaritySearch(
                    dslContext,
                    queryEmbedding.vector(),
                    limit,
                    similarityThreshold
            );
            if (matchedEntities.isEmpty()) {
                return Map.of("nodes", List.of(), "edges", List.of());
            }
            List<Long> entityIds = matchedEntities.stream()
                    .map(KgEntityPO::getId)
                    .toList();
            Map<String, Object> graphData = kgRelationRepository.queryGraphByEntityIds(entityIds, maxDepth);
            return graphData;
        });
    }

    /**
     * 根据RAG ID获取文档及其关联实体列表
     * 
     * @param ragId RAG ID
     * @return 文档列表，每个文档包含docId、文档详情和关联的实体列表
     */
    public List<Map<String, Object>> getDocsWithEntitiesByRagId(Long ragId) {
        // 从rag_doc表查询文档ID列表
        List<String> docIds = dslContextFactory.callable(dsl -> {
            return ragDocRepository.queryDocIdsByRagId(dsl, ragId);
        });
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String docId : docIds) {
            // 查询文档详情
            DocView docView = dslContextFactory.callable(dsl -> {
                return docRepository.queryById(dsl, docId);
            });
            // 查询关联的实体
            List<Map<String, Object>> entities = kgRelationRepository.queryEntitiesByDocId(docId);
            
            // 构建文档信息（不包含text字段）
            Map<String, Object> docInfo = new java.util.HashMap<>();
            docInfo.put("docId", docId);
            if (docView != null) {
                docInfo.put("name", docView.getName() != null ? docView.getName() : docId);
                docInfo.put("owner", docView.getOwner() != null ? docView.getOwner() : "");
            } else {
                docInfo.put("name", docId);
                docInfo.put("owner", "");
            }
            docInfo.put("entities", entities);
            
            result.add(docInfo);
        }
        
        return result;
    }

    /**
     * 根据文档ID获取关联的实体和句子
     * 从PostgreSQL查询实体信息，从Neo4j查询句子信息
     * 
     * @param docId 文档ID
     * @return 实体列表，包含实体ID、名称、类型和句子
     */
    public List<Map<String, Object>> getEntitiesByDocId(String docId) {
        return dslContextFactory.callable(dsl -> {
            // 1. 从PostgreSQL查询实体ID列表
            List<Long> entityIds = docKgEntityRepository.queryEntityIdsByDocId(dsl, docId);
            if (entityIds.isEmpty()) {
                return List.of();
            }
            
            // 2. 从PostgreSQL查询实体详细信息
            Map<Long, KgEntityPO> entityMap = new HashMap<>();
            for (Long entityId : entityIds) {
                KgEntityPO entity = kgEntityRepository.queryById(dsl, entityId);
                if (entity != null) {
                    entityMap.put(entityId, entity);
                }
            }
            
            // 3. 从Neo4j查询句子信息（句子存储在Neo4j的关系属性上）
            Map<Long, List<String>> sentencesMap = kgRelationRepository.querySentencesByDocIdAndEntityIds(docId, entityIds);
            
            // 4. 组装结果
            List<Map<String, Object>> result = new java.util.ArrayList<>();
            for (Long entityId : entityIds) {
                KgEntityPO entity = entityMap.get(entityId);
                if (entity != null) {
                    List<String> sentences = sentencesMap.getOrDefault(entityId, List.of());
                    result.add(Map.of(
                        "entityId", entity.getId(),
                        "entityName", entity.getName() != null ? entity.getName() : "",
                        "entityType", entity.getType() != null ? entity.getType() : "default",
                        "sentences", sentences
                    ));
                }
            }
            
            // 按实体名称排序
            result.sort((a, b) -> {
                String nameA = (String) a.get("entityName");
                String nameB = (String) b.get("entityName");
                return nameA.compareTo(nameB);
            });
            
            return result;
        });
    }

    /**
     * 根据实体ID和文档ID查询该实体在该文档中的实体关系图谱
     * 
     * @param entityId 实体ID
     * @param docId 文档ID
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> getEntityGraphInDoc(Long entityId, String docId) {
        return kgRelationRepository.queryEntityGraphInDoc(entityId, docId);
    }

    /**
     * 获取全部知识图谱：查询所有实体及其关系
     * 
     * @param maxDepth Neo4j查询深度
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> getAllKnowledgeGraph(int maxDepth) {
        return dslContextFactory.callable(dslContext -> {
            // 1. 查询所有实体
            List<KgEntityPO> allEntities = kgEntityRepository.queryAllEntities(dslContext);
            
            if (allEntities.isEmpty()) {
                return Map.of("nodes", List.of(), "edges", List.of());
            }
            
            // 2. 提取实体ID列表
            List<Long> entityIds = allEntities.stream()
                    .map(KgEntityPO::getId)
                    .toList();
            
            // 3. Neo4j查询：获取这些实体及其关系
            Map<String, Object> graphData = kgRelationRepository.queryGraphByEntityIds(entityIds, maxDepth);
            
            return graphData;
        });
    }

    public int deleteAllDocuments(Long ragId) {
        return dslContextFactory.callable(dslContext -> {
            // 删除RAG-文档关联
            int ragDocDeleted = ragDocRepository.deleteByRagId(dslContext, ragId);
            log.info("删除RAG-文档关联: ragId={}, 删除数量={}", ragId, ragDocDeleted);
            // 删除向量文档
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
     * @param embeddingId 文档embeddingId（UUID字符串）
     * @return 删除的文档数量
     */
    public int deleteDocument(String embeddingId) {
        return dslContextFactory.callable(dslContext -> {
            // 先查询文档，检查metadata中是否有docId
            // 注意：vector_document中的文档可能是通过uploadFile上传的，不是从doc表导入的
            // 只有从doc表导入的文档，metadata中才会有docId
            com.example.ddd.infrastructure.adapter.repository.VectorDocumentRepository repo = 
                    (com.example.ddd.infrastructure.adapter.repository.VectorDocumentRepository) vectorDocumentRepository;
            VectorDocumentEntity doc = repo.queryByEmbeddingId(dslContext, embeddingId);
            if (doc != null && doc.getMetadata() != null) {
                Object docIdObj = doc.getMetadata().get("docId");
                if (docIdObj != null && doc.getRagId() != null) {
                    String docId = docIdObj.toString();
                    // 删除RAG-文档关联
                    ragDocRepository.delete(dslContext, doc.getRagId(), docId);
                    log.info("删除RAG-文档关联: ragId={}, docId={}", doc.getRagId(), docId);
                }
            }
            // 删除向量文档
            return vectorDocumentRepository.deleteById(dslContext, embeddingId);
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


    public OpenAiChatModel createChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(openAIProperties.getKey())
                .baseUrl(openAIProperties.getUrl())
                .modelName(openAIProperties.getModel())
                .build();
    }

    public void importDocument(Long ragId, String docId) {
        dslContextFactory.execute((dsl) -> {
            ragDocRepository.insert(dsl, ragId, docId);
            log.info("成功插入RAG-文档关联: ragId={}, docId={}", ragId, docId);
            DocView docView = docRepository.queryById(dsl, docId);
            OpenAiChatModel chatModel = createChatModel();
            String prompt = """
                    你是一个信息抽取服务，从自然语言文本中抽取"实体"和"实体之间的关系（三元组）"。
                    请严格遵守以下要求：
                    1. 只根据提供的文本内容抽取，不要编造没有出现的事实。
                    2. 只输出 '{你的内容}' 输出必须是合法 JSON，且字段名固定：entities, relations。
                    3. entities 是数组，每个元素包含：
                       - name: 实体名称（字符串）
                       - type: 实体类型（字符串，常用值：Tech, System, Service, API, DB, Person, Org, Concept, Event 等）
                       - description: 对该实体的简短中文描述（可以为空字符串）
                       - sentences: 实体在文档中出现的句子列表（数组，从原文中提取包含该实体的完整句子，最多5句）
                    4. relations 是数组，每个元素包含：
                       - subject_name: 主体实体名称（要能在 entities 里找到对应 name）
                       - subject_type: 主体实体类型（要能在 entities 里找到对应 type）
                       - relation_type: 关系类型（字符串，推荐用大写下划线：USES_TECH, DEPENDS_ON, CALLS, WORKS_AT, BELONGS_TO, LOCATED_IN 等）
                       - object_name: 客体实体名称
                       - object_type: 客体实体类型
                       - evidence_text: 原文中支持这个关系的关键句子或短语（从文本中截取，尽量短）
                    5. 如果找不到任何实体或关系，输出空数组：{"entities": [], "relations": []}。
                    6. 不要输出任何解释性文字，只输出 不带代码块标注的JSON。
                    """;
            Map<String, String> messages = new HashMap<>();
            messages.put("systemPrompt", prompt);
            messages.put("userPrompt", """
                    请你把以下内容按照Systemprompt进行整合
                    内容：%s
                    """.formatted(docView.getText()));
            String chat = chatModel.chat(messages.toString());
            ExtractionResult extractionResult = Json.fromJson(chat, ExtractionResult.class);
            List<EntityDTO> entities = extractionResult.getEntities();
            EmbeddingModel embeddingModel = createEmbeddingModel();
            List<KgEntityPO> list = entities.stream().map(entity -> {
                KgEntityPO kgEntityPO = new KgEntityPO();
                kgEntityPO.setId(idGenerator.nextSnowflakeId()); // 使用Snowflake ID生成唯一ID
                kgEntityPO.setName(entity.getName());
                kgEntityPO.setType(entity.getType());
                kgEntityPO.setDescription(entity.getDescription());
                kgEntityPO.setEmbedding(embeddingModel.embed(TextSegment.from(entity.getName())).content().vector());
                return kgEntityPO;
            }).toList();
            kgEntityRepository.batchInsert(dsl, list);
            Map<String, Long> entityIdMap = new HashMap<>();
            Map<String, String> entityTypeMap = new HashMap<>();
            List<Long> entityIds = new java.util.ArrayList<>();
            for (KgEntityPO kgEntityPO : list) {
                entityIdMap.put(kgEntityPO.getName(), kgEntityPO.getId());
                entityTypeMap.put(kgEntityPO.getName(), kgEntityPO.getType());
                entityIds.add(kgEntityPO.getId());
            }
            
            // 插入文档-实体关联表
            if (!entityIds.isEmpty()) {
                docKgEntityRepository.batchInsert(dsl, docId, entityIds);
                log.info("成功插入文档-实体关联: docId={}, 实体数={}", docId, entityIds.size());
            }
            
            // 在Neo4j中建立doc->实体的关系，并存储实体出现的句子
            if (!entityIds.isEmpty()) {
                // 构建实体名称到ID和句子的映射
                Map<Long, List<String>> entitySentencesMap = new HashMap<>();
                for (int i = 0; i < entities.size(); i++) {
                    EntityDTO entity = entities.get(i);
                    Long entityId = entityIdMap.get(entity.getName());
                    if (entityId != null && entity.getSentences() != null && !entity.getSentences().isEmpty()) {
                        entitySentencesMap.put(entityId, entity.getSentences());
                    }
                }
                // 传递entityIdMap和entityTypeMap，确保实体节点在Neo4j中存在
                kgRelationRepository.createDocToEntitiesRelation(docId, ragId, entityIds, entitySentencesMap, entityIdMap, entityTypeMap);
                log.info("成功在Neo4j中建立文档-实体关系: docId={}, ragId={}, 实体数={}", docId, ragId, entityIds.size());
            }
            
            // 建立实体之间的关系
            List<RelationDTO> relations = extractionResult.getRelations();
            if (relations != null && !relations.isEmpty()) {
                kgRelationRepository.batchCreateRelations(entityIdMap, entityTypeMap, relations);
                log.info("成功导入文档到知识图谱: ragId={}, docId={}, 实体数={}, 关系数={}",
                        ragId, docId, entities.size(), relations.size());
            } else {
                log.info("成功导入文档到知识图谱: ragId={}, docId={}, 实体数={}, 关系数=0",
                        ragId, docId, entities.size());
            }
        });

    }
}

