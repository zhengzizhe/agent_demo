package com.example.ddd.application.kg.service;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.domain.agent.repository.KgEntityRepository;
import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.interfaces.request.KgEntityAddRequest;
import com.example.ddd.interfaces.request.KgEntityUpdateRequest;
import com.example.ddd.interfaces.response.KgEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识图谱实体服务
 * Service 层只能引用 Repository 层
 */
@Service
public class KgEntityService {

    @Autowired
    private KgEntityRepository kgEntityRepository;
    @Autowired
    private IdGenerator idGenerator;

    /**
     * 查询所有知识图谱实体
     */
    public List<KgEntityView> listAll() {
        return kgEntityRepository.queryAll();
    }

    /**
     * 根据ID查询知识图谱实体
     */
    public KgEntityView getById(String id) {
        return kgEntityRepository.queryViewById(id);
    }

    /**
     * 根据类型查询知识图谱实体
     */
    public List<KgEntityView> listByType(String type) {
        return kgEntityRepository.queryByType(type);
    }

    /**
     * 根据名称模糊查询知识图谱实体
     */
    public List<KgEntityView> searchByName(String name) {
        return kgEntityRepository.queryByNameLike(name);
    }

    /**
     * 添加知识图谱实体
     */
    public KgEntityView addEntity(KgEntityAddRequest request) {
        KgEntityPO kgEntityPO = new KgEntityPO();
        kgEntityPO.setId(idGenerator.nextId()); // 生成唯一ID（String类型）
        kgEntityPO.setName(request.getName());
        kgEntityPO.setType(request.getType());
        kgEntityPO.setDescription(request.getDescription());
        kgEntityPO.setEmbedding(request.getEmbedding());
        KgEntityPO result = kgEntityRepository.insert(kgEntityPO);
        return convertToView(result);
    }

    /**
     * 更新知识图谱实体
     */
    public KgEntityView updateEntity(String id, KgEntityUpdateRequest request) {
        KgEntityPO kgEntityPO = kgEntityRepository.queryById(id);

        if (kgEntityPO == null) {
            throw new IllegalArgumentException("未找到知识图谱实体: " + id);
        }

        if (request.getName() != null) {
            kgEntityPO.setName(request.getName());
        }
        if (request.getType() != null) {
            kgEntityPO.setType(request.getType());
        }
        if (request.getDescription() != null) {
            kgEntityPO.setDescription(request.getDescription());
        }
        if (request.getEmbedding() != null) {
            kgEntityPO.setEmbedding(request.getEmbedding());
        }

        kgEntityRepository.update(kgEntityPO);

        return convertToView(kgEntityPO);
    }

    /**
     * 删除知识图谱实体
     */
    public void deleteEntity(String id) {
        kgEntityRepository.deleteById(id);
    }

    /**
     * 将KgEntityPO转换为KgEntityView
     */
    private KgEntityView convertToView(KgEntityPO kgEntityPO) {
        KgEntityView view = new KgEntityView();
        view.setId(kgEntityPO.getId());
        view.setName(kgEntityPO.getName());
        view.setType(kgEntityPO.getType());
        view.setDescription(kgEntityPO.getDescription());
        return view;
    }
}

