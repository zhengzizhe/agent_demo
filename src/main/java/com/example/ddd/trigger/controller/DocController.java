package com.example.ddd.trigger.controller;

import com.example.ddd.domain.doc.service.DocService;
import com.example.ddd.trigger.request.DocAddRequest;
import com.example.ddd.trigger.response.DocView;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import java.util.List;

/**
 * 文档控制器
 */
@Controller("/doc")
public class DocController {
    @Inject
    DocService docService;

    /**
     * 根据userId查询所有文档
     */
    @Get("/list/{userId}")
    public List<DocView> list(@PathVariable("userId") String userId) {
        return docService.listByUserId(userId);
    }

    /**
     * 添加文档
     */
    @Post("/add")
    public DocView add(@Body DocAddRequest request) {
        return docService.addDocument(request);
    }
}
