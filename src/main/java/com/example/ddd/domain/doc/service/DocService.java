package com.example.ddd.domain.doc.service;

import com.example.ddd.infrastructure.adapter.repository.DocRepository;
import com.example.ddd.infrastructure.config.DSLContextFactory;
import com.example.ddd.infrastructure.dao.po.DocPO;
import com.example.ddd.trigger.request.DocAddRequest;
import com.example.ddd.trigger.response.DocView;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.UUID;

/**
 * 文档服务
 */
@Singleton
public class DocService {

    @Inject
    DSLContextFactory dslContext;
    @Inject
    private DocRepository docRepository;

    /**
     * 根据userId查询所有文档
     */
    public List<DocView> listByUserId(String userId) {
        return dslContext.callable((dsl) -> docRepository.queryByUserId(dsl, userId));
    }

    /**
     * 添加文档
     */
    public DocView addDocument(DocAddRequest request) {
        DocPO docPO = new DocPO();
        docPO.setId(UUID.randomUUID().toString().replace("-", "").substring(0, 32));
        docPO.setName(request.getName() != null ? request.getName() : "未命名文档");
        docPO.setText(request.getText() != null ? request.getText() : "");
        docPO.setType(request.getType() != null ? request.getType() : 0);
        docPO.setOwner(request.getUserId());
        DocPO result = dslContext.callable(dsl -> {
            return docRepository.insert(dslContext, docPO);
        });
        return convertToView(result);
    }

    /**
     * 将DocPO转换为DocView
     */
    private DocView convertToView(DocPO docPO) {
        DocView view = new DocView();
        view.setId(docPO.getId());
        view.setName(docPO.getName());
        // owner直接是userId字符串
        view.setOwner(docPO.getOwner());
        view.setText(docPO.getText());
        return view;
    }
}
