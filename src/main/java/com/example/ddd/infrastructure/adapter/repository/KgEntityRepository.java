package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.infrastructure.config.DSLContextFactory;
import com.example.ddd.infrastructure.dao.IKgEntityDao;
import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.trigger.response.KgEntityView;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

/**
 * 知识图谱实体仓储
 */
@Singleton
public class KgEntityRepository {
    @Inject
    IKgEntityDao kgEntityDao;

    public List<KgEntityView> queryAll(DSLContext dsl) {
        List<KgEntityPO> kgEntityPOList = kgEntityDao.queryAll(dsl);
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public KgEntityPO queryById(DSLContext dsl, Long id) {
        return kgEntityDao.queryById(dsl, id);
    }

    public KgEntityView queryViewById(DSLContext dsl, Long id) {
        KgEntityPO kgEntityPO = kgEntityDao.queryById(dsl, id);
        if (kgEntityPO == null) {
            return null;
        }
        return convertToView(kgEntityPO);
    }

    public List<KgEntityView> queryByType(DSLContext dsl, String type) {
        List<KgEntityPO> kgEntityPOList = kgEntityDao.queryByType(dsl, type);
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public List<KgEntityView> queryByNameLike(DSLContext dsl, String name) {
        List<KgEntityPO> kgEntityPOList = kgEntityDao.queryByNameLike(dsl, name);
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public KgEntityPO insert(DSLContextFactory dslContext, KgEntityPO kgEntityPO) {
        return kgEntityDao.insert(dslContext.createDsl(), kgEntityPO);
    }

    private KgEntityView convertToView(KgEntityPO kgEntityPO) {
        KgEntityView view = new KgEntityView();
        view.setId(kgEntityPO.getId());
        view.setName(kgEntityPO.getName());
        view.setType(kgEntityPO.getType());
        view.setDescription(kgEntityPO.getDescription());
        return view;
    }

    public void batchInsert(DSLContext dsl, List<KgEntityPO> list) {
        kgEntityDao.batchInsert(dsl, list);
    }

    /**
     * 向量相似度搜索
     */
    public List<KgEntityPO> similaritySearch(DSLContext dsl, float[] queryEmbedding, int limit, double similarityThreshold) {
        return kgEntityDao.similaritySearch(dsl, queryEmbedding, limit, similarityThreshold);
    }

    /**
     * 查询所有实体
     */
    public List<KgEntityPO> queryAllEntities(DSLContext dsl) {
        return kgEntityDao.queryAll(dsl);
    }
}

