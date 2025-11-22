package com.example.ddd.application.agent.service;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.application.agent.repository.IRagRepository;
import com.example.ddd.application.agent.repository.IVectorDocumentRepository;
import com.example.ddd.configuration.config.OpenAIProperties;
import com.example.ddd.domain.agent.model.entity.RagEntity;
import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.domain.agent.model.valobj.EntityDTO;
import com.example.ddd.domain.agent.model.valobj.ExtractionResult;
import com.example.ddd.domain.agent.model.valobj.RelationDTO;
import com.example.ddd.domain.agent.repository.*;

import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.interfaces.response.DocView;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Json;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务
 * 提供文档向量化、存储和检索功能
 */
@Service
@Slf4j
public class RagService {

    @Autowired
    private IRagRepository ragRepository;
    @Autowired
    private DocRepository docRepository;
    @Autowired
    private IVectorDocumentRepository vectorDocumentRepository;
    @Autowired
    private OpenAIProperties openAIProperties;
    @Autowired
    private KgEntityRepository kgEntityRepository;
    @Autowired
    private KgRelationRepository kgRelationRepository;
    @Autowired
    private DocKgEntityRepository docKgEntityRepository;
    @Autowired
    private RagDocRepository ragDocRepository;
    @Autowired
    private IdGenerator idGenerator;

    /**
     * 添加文档到RAG知识库
     *
     * @param ragId    RAG ID
     * @param text     文档文本
     * @param metadata 文档元数据
     * @return 添加的文档数量（始终为1）
     */
    @Transactional
    public int addDocument(String ragId, String text, Map<String, Object> metadata) {
        RagEntity rag = ragRepository.queryById(ragId);
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
        vectorDocumentRepository.insert(entity);
        log.info("文档已添加到RAG: ragId={}, insertedCount=1", ragId);
        return 1; // 始终返回1，表示添加了1个文档
    }

    public List<VectorDocumentEntity> search(String ragId, String queryText, int limit, double similarityThreshold) {
        RagEntity rag = ragRepository.queryById(ragId);
        if (rag == null) {
            throw new IllegalArgumentException("未找到RAG: " + ragId);
        }
        EmbeddingModel embeddingModel = createEmbeddingModel();
        TextSegment querySegment = TextSegment.from(queryText);
        Embedding queryEmbedding = embeddingModel.embed(querySegment).content();
        return vectorDocumentRepository.similaritySearch(
                ragId,
                queryEmbedding.vector(),
                limit,
                similarityThreshold
        );
    }

    /**
     * KG知识图谱搜索：先向量匹配，再Neo4j查询关系
     *
     * @param queryText           查询文本
     * @param limit               向量匹配返回数量
     * @param similarityThreshold 相似度阈值
     * @param maxDepth            Neo4j查询深度
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> searchKnowledgeGraph(String queryText, int limit, double similarityThreshold, int maxDepth) {
        // 1. 向量匹配：将查询文本转换为向量，在kg_entity表中搜索相似实体
        EmbeddingModel embeddingModel = createEmbeddingModel();
        TextSegment querySegment = TextSegment.from(queryText);
        Embedding queryEmbedding = embeddingModel.embed(querySegment).content();

        List<KgEntityPO> matchedEntities = kgEntityRepository.similaritySearch(
                queryEmbedding.vector(),
                limit,
                similarityThreshold
        );
        if (matchedEntities.isEmpty()) {
            return Map.of("nodes", List.of(), "edges", List.of());
        }
        List<String> entityIds = matchedEntities.stream()
                .map(KgEntityPO::getId)
                .toList();
        Map<String, Object> graphData = kgRelationRepository.queryGraphByEntityIds(entityIds, maxDepth);
        return graphData;
    }

    /**
     * 根据RAG ID获取文档及其关联实体列表
     *
     * @param ragId RAG ID
     * @return 文档列表，每个文档包含docId、文档详情和关联的实体列表
     */
    public List<Map<String, Object>> getDocsWithEntitiesByRagId(String ragId) {
        // 从rag_doc表查询文档ID列表
        List<String> docIds = ragDocRepository.queryDocIdsByRagId(ragId);
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String docId : docIds) {
            // 查询文档详情
            DocView docView = docRepository.queryById(docId);
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
        // 1. 从PostgreSQL查询实体ID列表
        List<String> entityIds = docKgEntityRepository.queryEntityIdsByDocId(docId);
        if (entityIds.isEmpty()) {
            return List.of();
        }

        // 2. 从PostgreSQL查询实体详细信息
        Map<String, KgEntityPO> entityMap = new HashMap<>();
        for (String entityId : entityIds) {
            KgEntityPO entity = kgEntityRepository.queryById(entityId);
            if (entity != null) {
                entityMap.put(entityId, entity);
            }
        }

        // 3. 从Neo4j查询句子信息（句子存储在Neo4j的关系属性上）
        Map<String, List<String>> sentencesMap = kgRelationRepository.querySentencesByDocIdAndEntityIds(docId, entityIds);

        // 4. 组装结果
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (String entityId : entityIds) {
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
    }

    /**
     * 根据实体ID和文档ID查询该实体在该文档中的实体关系图谱
     *
     * @param entityId 实体ID
     * @param docId    文档ID
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> getEntityGraphInDoc(String entityId, String docId) {
        return kgRelationRepository.queryEntityGraphInDoc(entityId, docId);
    }

    /**
     * 获取全部知识图谱：查询所有实体及其关系
     *
     * @param maxDepth Neo4j查询深度
     * @return 包含节点和边的图谱数据
     */
    public Map<String, Object> getAllKnowledgeGraph(int maxDepth) {
        // 1. 查询所有实体
        List<KgEntityPO> allEntities = kgEntityRepository.queryAllEntities();

        if (allEntities.isEmpty()) {
            return Map.of("nodes", List.of(), "edges", List.of());
        }

        // 2. 提取实体ID列表
        List<String> entityIds = allEntities.stream()
                .map(KgEntityPO::getId)
                .toList();

        // 3. Neo4j查询：获取这些实体及其关系
        Map<String, Object> graphData = kgRelationRepository.queryGraphByEntityIds(entityIds, maxDepth);

        return graphData;
    }

    @Transactional
    public int deleteAllDocuments(String ragId) {
        // 删除RAG-文档关联
        int ragDocDeleted = ragDocRepository.deleteByRagId(ragId);
        log.info("删除RAG-文档关联: ragId={}, 删除数量={}", ragId, ragDocDeleted);
        // 删除向量文档
        return vectorDocumentRepository.deleteByRagId(ragId);
    }

    /**
     * 根据RAG ID查询所有文档
     *
     * @param ragId RAG ID
     * @return 文档列表
     */
    public List<VectorDocumentEntity> queryDocuments(String ragId) {
        return vectorDocumentRepository.queryByRagId(ragId);
    }

    /**
     * 根据ID删除文档
     *
     * @param embeddingId 文档embeddingId（UUID字符串）
     * @return 删除的文档数量
     */
    @Transactional
    public int deleteDocument(String embeddingId) {
        // 先查询文档，检查metadata中是否有docId
        // 注意：vector_document中的文档可能是通过uploadFile上传的，不是从doc表导入的
        // 只有从doc表导入的文档，metadata中才会有docId
        com.example.ddd.domain.agent.repository.VectorDocumentRepository repo =
                (com.example.ddd.domain.agent.repository.VectorDocumentRepository) vectorDocumentRepository;
        VectorDocumentEntity doc = repo.queryByEmbeddingId(embeddingId);
        if (doc != null && doc.getMetadata() != null) {
            Object docIdObj = doc.getMetadata().get("docId");
            if (docIdObj != null && doc.getRagId() != null) {
                String docId = docIdObj.toString();
                // 删除RAG-文档关联
                ragDocRepository.delete(doc.getRagId(), docId);
                log.info("删除RAG-文档关联: ragId={}, docId={}", doc.getRagId(), docId);
            }
        }
        // 删除向量文档
        return vectorDocumentRepository.deleteById(embeddingId);
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

    @Transactional
    public void importDocument(String ragId, String docId) {
        ragDocRepository.insert(ragId, docId);
        log.info("成功插入RAG-文档关联: ragId={}, docId={}", ragId, docId);
        DocView docView = docRepository.queryById(docId);
            OpenAiChatModel chatModel = createChatModel();
            String prompt = """
                    你是一个专业的「信息抽取服务」，用于从自然语言文本中构建知识图谱。
                    你的任务是从给定文本中抽取「实体」和「实体之间的关系（三元组）」并返回 JSON。
                    
                    请严格遵守以下要求：
                    
                    1. 严格依据提供的文本内容抽取，不要引入文本中没有出现的事实或外部知识。
                    2. 最终输出必须是**合法 JSON 对象**，字段名固定为：entities, relations。
                       - 不能输出任何解释性文字、注释、说明、前后缀。
                       - 不能输出 Markdown 代码块标记（例如 ```json 或 ```）。
                       - 如果解析失败会导致系统报错，所以一定要保证 JSON 严格合法。
                    
                    ------------------------------
                    【实体抽取要求】
                    ------------------------------
                    
                    3. entities 是数组，每个元素包含以下字段：
                       - name: 实体名称（字符串，使用规范、完整的名词短语）
                       - type: 实体类型（字符串，优先从下面这些中选择，没有合适的再自定义）：
                           Tech, System, Service, API, DB, Table, Queue, Topic,
                           Module, Component, Job, Task,
                           Person, Role, Org,
                           Concept, Event, Rule, Config, Env, Metric, ErrorCode, Other
                       - description: 对该实体的简短中文描述（字符串，可以为空字符串）
                       - sentences: 该实体在文档中出现的句子列表（数组，直接从原文中截取完整句子，最多 5 句）
                    
                    4. 实体抽取原则：
                       - 使用**规范化名称**，避免碎片化。
                         - ❌ "系统的" → 不是实体
                         - ✔ "推荐系统" → 是实体
                       - 多个同义称呼指向同一实体时，请统一为一个 name，如：
                         - "AI" / "人工智能" → 统一为 "人工智能"
                       - 不要把代词（如"它"、"该系统"、"本文"）当作实体。
                       - 尽量抽取对理解系统、架构、业务流程有价值的实体，忽略噪音名词。
                    
                    ------------------------------
                    【关系抽取要求】
                    ------------------------------
                    
                    5. relations 是数组，每个元素包含：
                       - subject_name: 主体实体名称（要能在 entities 里找到对应 name）
                       - subject_type: 主体实体类型（要与 entities 中该实体的 type 一致）
                       - relation_type: 关系类型（字符串，必须为**大写下划线命名**）
                       - object_name: 客体实体名称（要能在 entities 里找到对应 name）
                       - object_type: 客体实体类型（要与 entities 中该实体的 type 一致）
                       - evidence_text: 原文中支持这个关系的关键句子或短语（从文本中截取，尽量短但能表达关系）
                    
                    6. relation_type 使用规范：
                       - 尽量从以下关系类型中选择（不强制，但强烈推荐统一）：
                           USES_TECH        // 使用某种技术/组件/工具
                           DEPENDS_ON       // 依赖于
                           CALLS_API        // 调用接口/方法
                           BELONGS_TO       // 从属关系（模块属于系统、表属于库等）
                           PART_OF          // 组成关系（是……的一部分）
                           IMPLEMENTS       // 实现某种接口/规范
                           EXTENDS          // 继承/扩展
                           PRODUCES         // 产生某种数据/消息/结果
                           CONSUMES         // 消费某种数据/消息
                           STORES_IN_DB     // 把数据存储在某个数据库或表中
                           RUNS_ON          // 运行在某个环境/平台上
                           COMMUNICATES_WITH// 与……通信
                           MANAGED_BY       // 由……管理/维护
                           OWNED_BY         // 归属于某个团队/组织
                           LOCATED_IN       // 位于某个环境/机房/区域
                           WORKS_AT         // 某人就职/工作于某组织
                           CAUSED_BY        // 由……导致
                           SOLVES           // 解决某个问题
                           ASSOCIATED_WITH  // 无法精确归类但存在较强关联时使用
                       
                       - 如果上述列表中都不合适，可以自定义新的 relation_type，
                         但仍然必须使用全大写 + 下划线，比如：HANDLES_ERROR, SCHEDULED_BY。
                    
                    7. 关系抽取原则：
                       - 关系可以来自**同一句**，也可以来自**同一段落中多句综合后的含义**，
                         但 evidence_text 必须能在原文中找到。
                       - 尽量抽取**对理解系统架构、数据流、调用链路有帮助**的关系，
                         比如"谁调用谁"、"谁依赖谁"、"数据存到哪里"、"由谁负责"等。
                       - 不要为了凑数而抽取非常抽象、泛泛而谈的关系。
                    
                    ------------------------------
                    【空结果约定】
                    ------------------------------
                    
                    8. 如果文本中确实找不到任何有价值的实体或关系，请返回：
                       {
                         "entities": [],
                         "relations": []
                       }
                    
                    ------------------------------
                    【输出格式要求（重要）】
                    ------------------------------
                    
                    9. 最终只输出一个 JSON 对象，形如：
                       {
                         "entities": [ ... ],
                         "relations": [ ... ]
                       }
                    
                    10. 不能输出除 JSON 以外的任何内容（例如"好的，结果如下："之类的文字）。
                    
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
                kgEntityPO.setId(idGenerator.nextId()); // 生成String类型的ULID
                kgEntityPO.setName(entity.getName());
                kgEntityPO.setType(entity.getType());
                kgEntityPO.setDescription(entity.getDescription());
                kgEntityPO.setEmbedding(embeddingModel.embed(TextSegment.from(entity.getName())).content().vector());
                return kgEntityPO;
            }).toList();
            kgEntityRepository.batchInsert(list);
            Map<String, String> entityIdMap = new HashMap<>();
            Map<String, String> entityTypeMap = new HashMap<>();
            List<String> entityIds = new java.util.ArrayList<>();
            for (KgEntityPO kgEntityPO : list) {
                entityIdMap.put(kgEntityPO.getName(), kgEntityPO.getId());
                entityTypeMap.put(kgEntityPO.getName(), kgEntityPO.getType());
                entityIds.add(kgEntityPO.getId());
            }
            if (!entityIds.isEmpty()) {
                docKgEntityRepository.batchInsert(docId, entityIds);
                log.info("成功插入文档-实体关联: docId={}, 实体数={}", docId, entityIds.size());
            }
            if (!entityIds.isEmpty()) {
                // 构建实体名称到ID和句子的映射
                Map<String, List<String>> entitySentencesMap = new HashMap<>();
                for (int i = 0; i < entities.size(); i++) {
                    EntityDTO entity = entities.get(i);
                    String entityId = entityIdMap.get(entity.getName());
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
    }
}
