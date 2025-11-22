package com.example.ddd.domain.agent.repository;

import com.example.ddd.infrastructure.dao.mapper.DocMapper;
import com.example.ddd.infrastructure.dao.po.DocPO;
import com.example.ddd.interfaces.response.DocView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocRepository {
    @Autowired
    private DocMapper docMapper;

    public List<DocView> queryByUserId(String userId) {
        List<DocPO> docPOS = docMapper.queryByUserId(userId);
        return docPOS.stream().map(docPO -> {
            DocView view = new DocView();
            view.setId(docPO.getId());
            view.setOwner(docPO.getOwner());
            view.setName(docPO.getName());
            view.setText(docPO.getText()); // 包含文档内容
            return view;
        }).toList();
    }

    public DocView queryById(String id) {
        DocPO docPO = docMapper.queryById(id);
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

    public DocPO insert(DocPO docPO) {
        docMapper.insert(docPO);
        return docPO;
    }
}
