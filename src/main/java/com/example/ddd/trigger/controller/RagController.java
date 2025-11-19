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
    @Status(HttpStatus.CREATED)
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
    @Status(HttpStatus.CREATED)
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

