package com.example.ddd.interfaces.controller;

import com.example.ddd.application.kg.service.KgEntityService;
import com.example.ddd.interfaces.request.KgEntityAddRequest;
import com.example.ddd.interfaces.request.KgEntityUpdateRequest;
import com.example.ddd.interfaces.response.KgEntityView;
import com.example.ddd.interfaces.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识图谱实体控制器
 */
@RestController
@RequestMapping("/kg-entity")
public class KgEntityController {
    @Autowired
    private KgEntityService kgEntityService;

    /**
     * 查询所有知识图谱实体
     */
    @GetMapping("/list")
    public Result<List<KgEntityView>> list() {
        return Result.success(kgEntityService.listAll());
    }

    /**
     * 根据ID查询知识图谱实体
     */
    @GetMapping("/{id}")
    public Result<KgEntityView> getById(@PathVariable("id") String id) {
        KgEntityView view = kgEntityService.getById(id);
        if (view == null) {
            return Result.notFound("未找到知识图谱实体: " + id);
        }
        return Result.success(view);
    }

    /**
     * 根据类型查询知识图谱实体
     */
    @GetMapping("/type/{type}")
    public Result<List<KgEntityView>> listByType(@PathVariable("type") String type) {
        return Result.success(kgEntityService.listByType(type));
    }

    /**
     * 根据名称搜索知识图谱实体
     */
    @GetMapping("/search")
    public Result<List<KgEntityView>> searchByName(@RequestParam("name") String name) {
        return Result.success(kgEntityService.searchByName(name));
    }

    /**
     * 添加知识图谱实体
     */
    @PostMapping("/add")
    public Result<KgEntityView> add(@RequestBody KgEntityAddRequest request) {
        return Result.success("添加成功", kgEntityService.addEntity(request));
    }

    /**
     * 更新知识图谱实体
     */
    @PutMapping("/{id}")
    public Result<KgEntityView> update(@PathVariable("id") String id, @RequestBody KgEntityUpdateRequest request) {
        try {
            return Result.success("更新成功", kgEntityService.updateEntity(id, request));
        } catch (IllegalArgumentException e) {
            return Result.notFound(e.getMessage());
        }
    }

    /**
     * 删除知识图谱实体
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        try {
            kgEntityService.deleteEntity(id);
            return Result.success("删除成功", null);
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}

