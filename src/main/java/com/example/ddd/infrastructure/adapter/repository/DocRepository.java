package com.example.ddd.infrastructure.adapter.repository;

import com.example.ddd.infrastructure.config.DSLContextFactory;
import com.example.ddd.infrastructure.dao.IDocDao;
import com.example.ddd.infrastructure.dao.po.DocPO;
import com.example.ddd.trigger.response.DocView;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

@Singleton
public class DocRepository {
    @Inject
    IDocDao docDao;

    public List<DocView> queryByUserId(DSLContext dsl, String userId) {
        List<DocPO> docPOS = docDao.queryByUserId(dsl, userId);
        return docPOS.stream().map(docPO -> {
            DocView view = new DocView();
            view.setId(docPO.getId());
            view.setOwner(docPO.getOwner());
            view.setName(docPO.getName());
            view.setText(docPO.getText()); // 包含文档内容
            return view;
        }).toList();
    }

    public DocView queryById(DSLContext dsl, String id) {
        DocPO docPO = docDao.queryById(dsl, id);
        if (docPO == null) {
            return null;
        }
        DocView view = new DocView();
        view.setId(docPO.getId());
        view.setOwner(docPO.getOwner());
        view.setName(docPO.getName());
        view.setText(docPO.getText());
        return view;
    }

    public DocPO insert(DSLContextFactory dslContext, DocPO docPO) {
        return docDao.insert(dslContext.createDsl(), docPO);
    }
}
