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
import java.util.Optional;

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
     * @param request 添加文档请求
     * @return 添加的文档块数量
     */
    @Post("/documents")
    @Status(HttpStatus.CREATED)
    public Map<String, Object> addDocument(@Body RagAddDocumentRequest request) {
        int segmentCount = ragService.addDocument(
            request.getRagId(),
            request.getText(),
            request.getMetadata()
        );
        return Map.of(
            "success", true,
            "ragId", request.getRagId() != null ? request.getRagId() : 1L,
            "segmentCount", segmentCount,
            "message", "文档添加成功"
        );
    }

    /**
     * 上传文件并导入到RAG知识库
     *
     * @param file 上传的文件
     * @param ragId RAG ID（可选，默认1）
     * @return 导入结果
     */
    @Post(value = "/documents/upload", consumes = "multipart/form-data", produces = "application/json")
    @Status(HttpStatus.CREATED)
    public Map<String, Object> uploadFile(
            @Part("file") CompletedFileUpload file,
            @Part(value = "ragId") Optional<Long> ragId) {
        try {
            // 验证文件
            if (file == null || file.getSize() == 0) {
                return Map.of(
                    "success", false,
                    "message", "文件不能为空"
                );
            }

            // 检查文件类型
            String contentType = file.getContentType().map(ct -> ct.toString()).orElse("application/octet-stream");
            String fileName = file.getFilename();
            log.info("上传文件: fileName={}, contentType={}, size={}", fileName, contentType, file.getSize());

            // 读取文件内容
            byte[] fileBytes = file.getBytes();
            String fileContent = new String(fileBytes, StandardCharsets.UTF_8);
            
            // 构建元数据
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("fileName", fileName);
            metadata.put("contentType", contentType);
            metadata.put("fileSize", file.getSize());
            metadata.put("source", "file_upload");

            // 添加到RAG
            Long finalRagId = ragId.orElse(1L);
            int segmentCount = ragService.addDocument(
                finalRagId,
                fileContent,
                metadata
            );

            return Map.of(
                "success", true,
                "ragId", finalRagId,
                "fileName", fileName,
                "segmentCount", segmentCount,
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

    /**
     * 搜索相似文档
     *
     * @param request 搜索请求
     * @return 相似文档列表
     */
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

