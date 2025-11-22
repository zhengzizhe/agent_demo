package com.example.ddd.interfaces.controller;

import com.example.ddd.application.doc.service.DocService;
import com.example.ddd.interfaces.request.DocAddRequest;
import com.example.ddd.interfaces.response.DocView;
import com.example.ddd.interfaces.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文档控制器
 */
@RestController
@RequestMapping("/doc")
public class DocController {
    @Autowired
    private DocService docService;

    /**
     * 根据userId查询所有文档
     */
    @GetMapping("/list/{userId}")
    public Result<List<DocView>> list(@PathVariable("userId") String userId) {
        return Result.success(docService.listByUserId(userId));
    }

    /**
     * 添加文档
     */
    @PostMapping("/add")
    public Result<DocView> add(@RequestBody DocAddRequest request) {
        return Result.success("添加成功", docService.addDocument(request));
    }
}
