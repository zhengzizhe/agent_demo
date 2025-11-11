package com.example.ddd.trigger.controller;

import com.example.ddd.trigger.request.RagAddDocumentRequest;
import com.example.ddd.trigger.request.RagSearchRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RAG Controller 测试类
 */
@MicronautTest
@SuppressWarnings({"unchecked", "rawtypes"})
class RagControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Test
    @SuppressWarnings("unchecked")
    void testHealth() {
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.GET("/rag/health"),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertEquals("UP", body.get("status"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testAddDocument() {
        RagAddDocumentRequest request = new RagAddDocumentRequest();
        request.setRagId(1L);
        request.setText("台湖第一人是于泽龙。");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("source", "test");
        metadata.put("author", "test-author");
        request.setMetadata(metadata);

        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/documents", request),
            Map.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("segmentCount"));
        assertTrue(((Number) body.get("segmentCount")).intValue() > 0);
    }

    @Test
    void testAddDocument_EmptyText() {
        RagAddDocumentRequest request = new RagAddDocumentRequest();
        request.setRagId(1L);
        request.setText("");

        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/documents", request),
            Map.class
        );

        // 空文本应该也能处理，返回0个segment
        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
    }

    @Test
    void testAddDocument_LongText() {
        // 测试长文本，应该被分成多个chunks
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            longText.append("这是一个测试段落。").append(i).append(" ");
            longText.append("它包含了一些测试内容，用于验证RAG系统的文档添加功能。");
            longText.append("这段文本应该会被分成多个chunks。");
        }

        RagAddDocumentRequest request = new RagAddDocumentRequest();
        request.setRagId(1L);
        request.setText(longText.toString());

        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/documents", request),
            Map.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
        // 长文本应该被分成多个segments
        assertTrue(((Number) body.get("segmentCount")).intValue() > 1);
    }

    @Test
    void testSearch() {

        // 搜索
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setRagId(1L);
        searchRequest.setQueryText("台湖第一人是谁");
        searchRequest.setLimit(5);
        searchRequest.setSimilarityThreshold(0.6);

        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/search", searchRequest),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("results"));
    }

    @Test
    void testSearch_EmptyQuery() {
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setRagId(1L);
        searchRequest.setQueryText("");

        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/search", searchRequest),
            Map.class
        );

        // 空查询应该也能处理
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
    }

    @Test
    void testDeleteAllDocuments() {
        // 先添加一个文档
        RagAddDocumentRequest addRequest = new RagAddDocumentRequest();
        addRequest.setRagId(1L);
        addRequest.setText("这是要删除的测试文档。");
        
        client.toBlocking().exchange(
            HttpRequest.POST("/rag/documents", addRequest),
            Map.class
        );

        // 等待一下
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 删除所有文档
        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.DELETE("/rag/documents/1"),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
        assertNotNull(body.get("deletedCount"));
    }

    @Test
    void testAddDocument_WithMetadata() {
        RagAddDocumentRequest request = new RagAddDocumentRequest();
        request.setRagId(1L);
        request.setText("带元数据的测试文档");
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("title", "测试标题");
        metadata.put("category", "测试分类");
        metadata.put("tags", new String[]{"tag1", "tag2"});
        request.setMetadata(metadata);

        @SuppressWarnings("unchecked")
        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/documents", request),
            Map.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void testSearch_WithCustomLimit() {
        RagSearchRequest searchRequest = new RagSearchRequest();
        searchRequest.setRagId(1L);
        searchRequest.setQueryText("测试");
        searchRequest.setLimit(3);
        searchRequest.setSimilarityThreshold(0.5);

        HttpResponse<Map> response = client.toBlocking().exchange(
            HttpRequest.POST("/rag/search", searchRequest),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
        Map<String, Object> body = (Map<String, Object>) response.getBody().orElse(new HashMap<>());
        assertTrue((Boolean) body.get("success"));
        // 验证limit参数生效
        assertTrue(((Number) body.get("count")).intValue() <= 3);
    }
}

