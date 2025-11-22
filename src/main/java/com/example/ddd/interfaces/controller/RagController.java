package com.example.ddd.interfaces.controller;

import com.example.ddd.domain.agent.model.entity.VectorDocumentEntity;
import com.example.ddd.application.agent.service.RagService;
import com.example.ddd.interfaces.request.RagAddDocumentRequest;
import com.example.ddd.interfaces.request.RagSearchRequest;
import com.example.ddd.interfaces.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG知识库管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/rag")
public class RagController {
    
    @Autowired
    private RagService ragService;

    /**
     * 添加文档到RAG知识库
     *
     * @param ragId   RAG ID（路径参数）
     * @param request 添加文档请求
     * @return 添加结果
     */
    @PostMapping("/{ragId}/documents")
    public Result<Map<String, Object>> addDocument(@PathVariable String ragId, @RequestBody RagAddDocumentRequest request) {
        try {
            int documentCount = ragService.addDocument(
                    ragId,
                    request.getText(),
                    request.getMetadata()
            );
            return Result.success("文档添加成功", Map.of(
                    "ragId", ragId,
                    "documentCount", documentCount
            ));
        } catch (Exception e) {
            log.error("添加文档失败", e);
            return Result.error("添加文档失败: " + e.getMessage());
        }
    }

    /**
     * 上传文件并导入到RAG知识库
     *
     * @param ragId RAG ID（路径参数）
     * @param file  上传的文件
     * @return 导入结果
     */
    @PostMapping(value = "/{ragId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, Object>> uploadFile(
            @PathVariable String ragId,
            @RequestPart("file") MultipartFile file) {
        try {
            // 验证文件
            if (file == null || file.isEmpty()) {
                return Result.badRequest("文件不能为空");
            }
            String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
            String fileName = file.getOriginalFilename();
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
            return Result.success("文件导入成功", Map.of(
                    "ragId", ragId,
                    "fileName", fileName,
                    "documentCount", documentCount
            ));
        } catch (IOException e) {
            log.error("文件上传处理失败", e);
            return Result.error("文件处理失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("文件导入失败", e);
            return Result.error("文件导入失败: " + e.getMessage());
        }
    }

    /**
     * RAG搜索
     */
    @PostMapping("/search")
    public Result<Map<String, Object>> search(@RequestBody RagSearchRequest request) {
        try {
            List<VectorDocumentEntity> results = ragService.search(
                    request.getRagId(),
                    request.getQueryText(),
                    request.getLimit() != null ? request.getLimit() : 10,
                    request.getSimilarityThreshold() != null ? request.getSimilarityThreshold() : 0.6
            );
            return Result.success(Map.of(
                    "ragId", request.getRagId() != null ? request.getRagId() : "",
                    "queryText", request.getQueryText(),
                    "results", results,
                    "count", results.size()
            ));
        } catch (Exception e) {
            log.error("RAG搜索失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 删除RAG知识库中的所有文档
     *
     * @param ragId RAG ID
     * @return 删除的文档数量
     */
    @DeleteMapping("/documents/{ragId}")
    @ResponseStatus(HttpStatus.OK)
    public Result<Map<String, Object>> deleteAllDocuments(@PathVariable String ragId) {
        try {
            int deletedCount = ragService.deleteAllDocuments(ragId);
            return Result.success("文档删除成功", Map.of(
                    "ragId", ragId,
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 查询RAG知识库中的所有文档
     *
     * @param ragId RAG ID
     * @return 文档列表
     */
    @GetMapping("/documents/{ragId}")
    public Result<Map<String, Object>> queryDocuments(@PathVariable String ragId) {
        try {
            List<VectorDocumentEntity> documents = ragService.queryDocuments(ragId);
            return Result.success(Map.of(
                    "ragId", ragId,
                    "documents", documents,
                    "count", documents.size()
            ));
        } catch (Exception e) {
            log.error("查询文档失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID删除文档
     *
     * @param embeddingId 文档embeddingId（UUID字符串）
     * @return 删除结果
     */
    @DeleteMapping("/documents/doc/{embeddingId}")
    @ResponseStatus(HttpStatus.OK)
    public Result<Map<String, Object>> deleteDocument(@PathVariable String embeddingId) {
        try {
            int deletedCount = ragService.deleteDocument(embeddingId);
            return Result.success("文档删除成功", Map.of(
                    "embeddingId", embeddingId,
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 导入文档到RAG知识库
     */
    @PostMapping("/{ragId}/documents/import")
    public Result<Map<String, Object>> importDocument(
            @PathVariable String ragId,
            @RequestBody Map<String, String> request) {
        try {
            String docId = request.get("docId");
            ragService.importDocument(ragId, docId);
            return Result.success("文档导入成功", Map.of(
                    "ragId", ragId,
                    "docId", docId != null ? docId : ""
            ));
        } catch (Exception e) {
            log.error("导入文档失败", e);
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * KG知识图谱搜索
     *
     * @param request 查询请求（包含queryText、limit、similarityThreshold、maxDepth）
     * @return 图谱数据（包含节点和边）
     */
    @PostMapping("/kg/search")
    public Result<Map<String, Object>> searchKnowledgeGraph(@RequestBody Map<String, Object> request) {
        try {
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
            
            return Result.success(Map.of(
                    "queryText", queryText != null ? queryText : "",
                    "graph", graphData
            ));
        } catch (Exception e) {
            log.error("知识图谱搜索失败", e);
            return Result.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 获取全部知识图谱
     *
     * @param maxDepth Neo4j查询深度（默认1）
     * @return 图谱数据（包含节点和边）
     */
    @GetMapping("/kg/all")
    public Result<Map<String, Object>> getAllKnowledgeGraph(
            @RequestParam(value = "maxDepth", defaultValue = "1") Integer maxDepth) {
        try {
            Map<String, Object> graphData = ragService.getAllKnowledgeGraph(maxDepth);
            return Result.success(Map.of("graph", graphData));
        } catch (Exception e) {
            log.error("获取知识图谱失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据RAG ID获取文档及其关联实体列表
     *
     * @param ragId RAG ID
     * @return 文档列表，每个文档包含docId和关联的实体列表
     */
    @GetMapping("/{ragId}/documents/kg")
    public Result<Map<String, Object>> getDocsWithEntitiesByRagId(@PathVariable String ragId) {
        try {
            List<Map<String, Object>> docs = ragService.getDocsWithEntitiesByRagId(ragId);
            return Result.success(Map.of(
                    "ragId", ragId,
                    "docs", docs,
                    "count", docs.size()
            ));
        } catch (Exception e) {
            log.error("获取文档实体列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据文档ID获取关联的实体和句子
     *
     * @param docId 文档ID
     * @return 实体列表，包含实体ID、名称、类型和句子
     */
    @GetMapping("/kg/doc/{docId}/entities")
    public Result<Map<String, Object>> getEntitiesByDocId(@PathVariable String docId) {
        try {
            List<Map<String, Object>> entities = ragService.getEntitiesByDocId(docId);
            return Result.success(Map.of(
                    "docId", docId,
                    "entities", entities,
                    "count", entities.size()
            ));
        } catch (Exception e) {
            log.error("获取实体列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据实体ID和文档ID查询该实体在该文档中的实体关系图谱
     *
     * @param entityId 实体ID
     * @param docId 文档ID
     * @return 包含节点和边的图谱数据
     */
    @GetMapping("/kg/doc/{docId}/entity/{entityId}/graph")
    public Result<Map<String, Object>> getEntityGraphInDoc(
            @PathVariable String docId,
            @PathVariable String entityId) {
        try {
            Map<String, Object> graphData = ragService.getEntityGraphInDoc(entityId, docId);
            return Result.success(Map.of(
                    "docId", docId,
                    "entityId", entityId,
                    "graph", graphData
            ));
        } catch (Exception e) {
            log.error("获取实体图谱失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "service", "RAG Service",
                "message", "RAG服务运行正常"
        );
    }
}
