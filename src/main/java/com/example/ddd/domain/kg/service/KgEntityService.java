package com.example.ddd.domain.kg.service;

import com.example.ddd.common.utils.IdGenerator;
import com.example.ddd.infrastructure.adapter.repository.KgEntityRepository;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import com.example.ddd.infrastructure.dao.IKgEntityDao;
import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.trigger.request.KgEntityAddRequest;
import com.example.ddd.trigger.request.KgEntityUpdateRequest;
import com.example.ddd.trigger.response.KgEntityView;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * 知识图谱实体服务
 */
@Singleton
public class KgEntityService {

    @Inject
    DSLContextFactory dslContext;
    @Inject
    private KgEntityRepository kgEntityRepository;
    @Inject
    private IKgEntityDao kgEntityDao;
    @Inject
    private IdGenerator idGenerator;

    /**
     * 查询所有知识图谱实体
     */
    public List<KgEntityView> listAll() {
        return dslContext.callable((dsl) -> kgEntityRepository.queryAll(dsl));
    }

    /**
     * 根据ID查询知识图谱实体
     */
    public KgEntityView getById(Long id) {
        return dslContext.callable((dsl) -> kgEntityRepository.queryViewById(dsl, id));
    }

    /**
     * 根据类型查询知识图谱实体
     */
    public List<KgEntityView> listByType(String type) {
        return dslContext.callable((dsl) -> kgEntityRepository.queryByType(dsl, type));
    }

    /**
     * 根据名称模糊查询知识图谱实体
     */
    public List<KgEntityView> searchByName(String name) {
        return dslContext.callable((dsl) -> kgEntityRepository.queryByNameLike(dsl, name));
    }

    /**
     * 添加知识图谱实体
     */
    public KgEntityView addEntity(KgEntityAddRequest request) {
        KgEntityPO kgEntityPO = new KgEntityPO();
        kgEntityPO.setId(idGenerator.nextSnowflakeId()); // 生成唯一ID
        kgEntityPO.setName(request.getName());
        kgEntityPO.setType(request.getType());
        kgEntityPO.setDescription(request.getDescription());
        kgEntityPO.setEmbedding(request.getEmbedding());
        KgEntityPO result = dslContext.callable(dsl -> kgEntityRepository.insert(dslContext, kgEntityPO));
        return convertToView(result);
    }

    /**
     * 更新知识图谱实体
     */
    public KgEntityView updateEntity(Long id, KgEntityUpdateRequest request) {
        KgEntityPO kgEntityPO = dslContext.callable(dsl -> {
            return kgEntityDao.queryById(dsl, id);
        });

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

        dslContext.callable(dsl -> {
            kgEntityDao.update(dsl, kgEntityPO);
            return null;
        });

        return convertToView(kgEntityPO);
    }

    /**
     * 删除知识图谱实体
     */
    public void deleteEntity(Long id) {
        dslContext.callable(dsl -> {
            kgEntityDao.deleteById(dsl, id);
            return null;
        });
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

