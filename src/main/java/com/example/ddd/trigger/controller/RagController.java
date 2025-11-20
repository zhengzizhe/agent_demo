package com.example.ddd.trigger.controller;

import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.domain.agent.service.RagService;
import com.example.ddd.trigger.request.RagAddDocumentRequest;
import com.example.ddd.trigger.request.RagSearchRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG知识库管理Controller
 */
@Slf4j
@Controller("/rag")
public class RagController {
    @Inject
    private RagService ragService;

    /**
     * 添加文档到RAG知识库
     *
     * @param ragId   RAG ID（路径参数）
     * @param request 添加文档请求
     * @return 添加结果
     */
    @Post("/{ragId}/documents")
    public Map<String, Object> addDocument(@PathVariable Long ragId, @Body RagAddDocumentRequest request) {
        int documentCount = ragService.addDocument(
                ragId,
                request.getText(),
                request.getMetadata()
        );
        return Map.of(
                "success", true,
                "ragId", ragId,
                "documentCount", documentCount,
                "message", "文档添加成功"
        );
    }

    /**
     * 上传文件并导入到RAG知识库
     *
     * @param ragId RAG ID（路径参数）
     * @param file  上传的文件
     * @return 导入结果
     */
    @Post(value = "/{ragId}/documents/upload", consumes = "multipart/form-data", produces = "application/json")
    public Map<String, Object> uploadFile(
            @PathVariable Long ragId,
            @Part("file") CompletedFileUpload file) {
        try {
            // 验证文件
            if (file == null || file.getSize() == 0) {
                return Map.of(
                        "success", false,
                        "message", "文件不能为空"
                );
            }
            String contentType = file.getContentType().map(ct -> ct.toString()).orElse("application/octet-stream");
            String fileName = file.getFilename();
            log.info("上传文件: ragId={}, fileName={}, contentType={}, size={}", ragId, fileName, contentType, file.getSize());
            byte[] fileBytes = file.getBytes();
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileName", fileName);
            metadata.put("contentType", contentType);
            metadata.put("fileSize", file.getSize());
            metadata.put("source", "file_upload");
            // 添加到RAG
            int documentCount = ragService.addDocument(
                    ragId,
                    fileContent,
                    metadata
            );
            return Map.of(
                    "success", true,
                    "ragId", ragId,
                    "fileName", fileName,
                    "documentCount", documentCount,
                    "message", "文件导入成功"
            );
        } catch (IOException e) {
            log.error("文件上传处理失败", e);
            return Map.of(
                    "success", false,
                    "message", "文件处理失败: " + e.getMessage()
            );
        } catch (Exception e) {
            log.error("文件导入失败", e);
            return Map.of(
                    "success", false,
                    "message", "文件导入失败: " + e.getMessage()
            );
        }
    }

    @Post("/search")
    public Map<String, Object> search(@Body RagSearchRequest request) {
        List<VectorDocumentEntity> results = ragService.search(
                request.getRagId(),
                request.getQueryText(),
                request.getLimit() != null ? request.getLimit() : 10,
                request.getSimilarityThreshold() != null ? request.getSimilarityThreshold() : 0.6
        );
        return Map.of(
                "success", true,
                "ragId", request.getRagId() != null ? request.getRagId() : 1L,
                "queryText", request.getQueryText(),
                "results", results,
                "count", results.size()
        );
    }

    /**
     * 删除RAG知识库中的所有文档
     *
     * @param ragId RAG ID
     * @return 删除的文档数量
     */
    @Delete("/documents/{ragId}")
    @Status(HttpStatus.OK)
    public Map<String, Object> deleteAllDocuments(@PathVariable Long ragId) {
        int deletedCount = ragService.deleteAllDocuments(ragId);
        return Map.of(
                "success", true,
                "ragId", ragId,
                "deletedCount", deletedCount,
                "message", "文档删除成功"
        );
    }

    /**
     * 查询RAG知识库中的所有文档
     *
     * @param ragId RAG ID
     * @return 文档列表
     */
    @Get("/documents/{ragId}")
    public Map<String, Object> queryDocuments(@PathVariable Long ragId) {
        List<VectorDocumentEntity> documents = ragService.queryDocuments(ragId);
        return Map.of(
                "success", true,
                "ragId", ragId,
                "documents", documents,
                "count", documents.size()
        );
    }

    /**
     * 根据ID删除文档
     *
     * @param embeddingId 文档embeddingId（UUID字符串）
     * @return 删除结果
     */
    @Delete("/documents/doc/{embeddingId}")
    @Status(HttpStatus.OK)
    public Map<String, Object> deleteDocument(@PathVariable String embeddingId) {
        int deletedCount = ragService.deleteDocument(embeddingId);
        return Map.of(
                "success", true,
                "embeddingId", embeddingId,
                "deletedCount", deletedCount,
                "message", "文档删除成功"
        );
    }


    @Post("/{ragId}/documents/import")
    public Map<String, Object> importDocument(
            @PathVariable Long ragId,
            @Body Map<String, String> request) {
        String docId = request.get("docId");

        ragService.importDocument(
                ragId,
                docId
        );

        return Map.of(
                "success", true,
                "ragId", ragId,
                "docId", docId != null ? docId : "",
                "message", "文档导入成功"
        );
    }

    /**
     * KG知识图谱搜索
     *
     * @param queryText 查询文本
     * @param limit 向量匹配返回数量（默认10）
     * @param similarityThreshold 相似度阈值（默认0.6）
     * @param maxDepth Neo4j查询深度（默认1）
     * @return 图谱数据（包含节点和边）
     */
    @Post("/kg/search")
    public Map<String, Object> searchKnowledgeGraph(
            @Body Map<String, Object> request) {
        String queryText = (String) request.get("queryText");
        Integer limit = request.get("limit") != null ? (Integer) request.get("limit") : 10;
        Double similarityThreshold = request.get("similarityThreshold") != null ? 
                ((Number) request.get("similarityThreshold")).doubleValue() : 0.6;
        Integer maxDepth = request.get("maxDepth") != null ? (Integer) request.get("maxDepth") : 1;
        
        Map<String, Object> graphData = ragService.searchKnowledgeGraph(
                queryText,
                limit,
                similarityThreshold,
                maxDepth
        );
        
        return Map.of(
                "success", true,
                "queryText", queryText != null ? queryText : "",
                "graph", graphData
        );
    }

    /**
     * 获取全部知识图谱
     *
     * @param maxDepth Neo4j查询深度（默认1）
     * @return 图谱数据（包含节点和边）
     */
    @Get("/kg/all")
    public Map<String, Object> getAllKnowledgeGraph(
            @QueryValue(value = "maxDepth", defaultValue = "1") Integer maxDepth) {
        Map<String, Object> graphData = ragService.getAllKnowledgeGraph(maxDepth);
        
        return Map.of(
                "success", true,
                "graph", graphData
        );
    }

    /**
     * 根据RAG ID获取文档及其关联实体列表
     *
     * @param ragId RAG ID
     * @return 文档列表，每个文档包含docId和关联的实体列表
     */
    @Get("/{ragId}/documents/kg")
    public Map<String, Object> getDocsWithEntitiesByRagId(@PathVariable Long ragId) {
        List<Map<String, Object>> docs = ragService.getDocsWithEntitiesByRagId(ragId);
        return Map.of(
                "success", true,
                "ragId", ragId,
                "docs", docs,
                "count", docs.size()
        );
    }

    /**
     * 根据文档ID获取关联的实体和句子
     *
     * @param docId 文档ID
     * @return 实体列表，包含实体ID、名称、类型和句子
     */
    @Get("/kg/doc/{docId}/entities")
    public Map<String, Object> getEntitiesByDocId(@PathVariable String docId) {
        List<Map<String, Object>> entities = ragService.getEntitiesByDocId(docId);
        return Map.of(
                "success", true,
                "docId", docId,
                "entities", entities,
                "count", entities.size()
        );
    }

    /**
     * 根据实体ID和文档ID查询该实体在该文档中的实体关系图谱
     *
     * @param entityId 实体ID
     * @param docId 文档ID
     * @return 包含节点和边的图谱数据
     */
    @Get("/kg/doc/{docId}/entity/{entityId}/graph")
    public Map<String, Object> getEntityGraphInDoc(
            @PathVariable String docId,
            @PathVariable Long entityId) {
        Map<String, Object> graphData = ragService.getEntityGraphInDoc(entityId, docId);
        return Map.of(
                "success", true,
                "docId", docId,
                "entityId", entityId,
                "graph", graphData
        );
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @Get("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "RAG Service",
                "message", "RAG服务运行正常"
        );
    }
}

