package com.example.ddd.trigger.controller;

import com.example.ddd.domain.kg.service.KgEntityService;
import com.example.ddd.trigger.request.KgEntityAddRequest;
import com.example.ddd.trigger.request.KgEntityUpdateRequest;
import com.example.ddd.trigger.response.KgEntityView;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;

import java.util.List;

/**
 * 知识图谱实体控制器
 */
@Controller("/kg-entity")
public class KgEntityController {
    @Inject
    KgEntityService kgEntityService;

    /**
     * 查询所有知识图谱实体
     */
    @Get("/list")
    public List<KgEntityView> list() {
        return kgEntityService.listAll();
    }

    /**
     * 根据ID查询知识图谱实体
     */
    @Get("/{id}")
    public KgEntityView getById(@PathVariable("id") Long id) {
        return kgEntityService.getById(id);
    }

    /**
     * 根据类型查询知识图谱实体
     */
    @Get("/type/{type}")
    public List<KgEntityView> listByType(@PathVariable("type") String type) {
        return kgEntityService.listByType(type);
    }

    /**
     * 根据名称搜索知识图谱实体
     */
    @Get("/search")
    public List<KgEntityView> searchByName(@QueryValue("name") String name) {
        return kgEntityService.searchByName(name);
    }

    /**
     * 添加知识图谱实体
     */
    @Post("/add")
    @Status(HttpStatus.CREATED)
    public KgEntityView add(@Body KgEntityAddRequest request) {
        return kgEntityService.addEntity(request);
    }

    /**
     * 更新知识图谱实体
     */
    @Put("/{id}")
    public KgEntityView update(@PathVariable("id") Long id, @Body KgEntityUpdateRequest request) {
        return kgEntityService.updateEntity(id, request);
    }

    /**
     * 删除知识图谱实体
     */
    @Delete("/{id}")
    @Status(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        kgEntityService.deleteEntity(id);
    }
}

