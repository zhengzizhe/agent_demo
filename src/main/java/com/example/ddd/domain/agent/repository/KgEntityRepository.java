package com.example.ddd.domain.agent.repository;

import com.example.ddd.infrastructure.dao.mapper.KgEntityMapper;
import com.example.ddd.infrastructure.dao.po.KgEntityPO;
import com.example.ddd.interfaces.response.KgEntityView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 知识图谱实体仓储
 */
@Repository
public class KgEntityRepository {
    @Autowired
    private KgEntityMapper kgEntityMapper;

    public List<KgEntityView> queryAll() {
        List<KgEntityPO> kgEntityPOList = kgEntityMapper.queryAll();
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public KgEntityPO queryById(String id) {
        return kgEntityMapper.queryById(id);
    }

    public KgEntityView queryViewById(String id) {
        KgEntityPO kgEntityPO = kgEntityMapper.queryById(id);
        if (kgEntityPO == null) {
            return null;
        }
        return convertToView(kgEntityPO);
    }

    public List<KgEntityView> queryByType(String type) {
        List<KgEntityPO> kgEntityPOList = kgEntityMapper.queryByType(type);
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public List<KgEntityView> queryByNameLike(String name) {
        List<KgEntityPO> kgEntityPOList = kgEntityMapper.queryByNameLike(name);
        return kgEntityPOList.stream().map(this::convertToView).toList();
    }

    public KgEntityPO insert(KgEntityPO kgEntityPO) {
        kgEntityMapper.insert(kgEntityPO);
        return kgEntityPO;
    }

    private KgEntityView convertToView(KgEntityPO kgEntityPO) {
        KgEntityView view = new KgEntityView();
        view.setId(kgEntityPO.getId());
        view.setName(kgEntityPO.getName());
        view.setType(kgEntityPO.getType());
        view.setDescription(kgEntityPO.getDescription());
        return view;
    }

    public void batchInsert(List<KgEntityPO> list) {
        kgEntityMapper.batchInsert(list);
    }

    /**
     * 向量相似度搜索
     */
    public List<KgEntityPO> similaritySearch(float[] queryEmbedding, int limit, double similarityThreshold) {
        String embeddingStr = arrayToString(queryEmbedding);
        return kgEntityMapper.similaritySearch(embeddingStr, limit, similarityThreshold);
    }

    /**
     * 查询所有实体
     */
    public List<KgEntityPO> queryAllEntities() {
        return kgEntityMapper.queryAll();
    }

    /**
     * 更新知识图谱实体
     */
    public void update(KgEntityPO kgEntityPO) {
        kgEntityMapper.update(kgEntityPO);
    }

    /**
     * 根据ID删除知识图谱实体
     */
    public void deleteById(String id) {
        kgEntityMapper.deleteById(id);
    }

    /**
     * 将float[]转换为pgvector格式的字符串: "[0.1,0.2,0.3]"
     */
    private String arrayToString(float[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(array[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}

