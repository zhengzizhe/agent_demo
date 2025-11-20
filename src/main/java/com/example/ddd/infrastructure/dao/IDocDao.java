package com.example.ddd.infrastructure.dao;

import com.example.ddd.infrastructure.dao.po.DocPO;
import jakarta.inject.Singleton;
import org.jooq.DSLContext;

import java.util.List;

import static com.example.jooq.tables.Doc.DOC;

/**
 * 文档数据访问对象
 */
@Singleton
public class IDocDao {

    /**
     * 插入文档
     */
    public DocPO insert(DSLContext dslContext, DocPO docPO) {
        return dslContext.insertInto(DOC)
                .set(DOC.ID, docPO.getId())
                .set(DOC.NAME, docPO.getName())
                .set(DOC.OWNER, docPO.getOwner())
                .set(DOC.TEXT, docPO.getText())
                .set(DOC.TYPE, docPO.getType())
                .returning()
                .fetchOneInto(DocPO.class);
    }

    /**
     * 根据userId查询所有文档
     * owner字段直接存储userId（作为字符串）
     */
    public List<DocPO> queryByUserId(DSLContext dslContext, String userId) {
        return dslContext.selectFrom(DOC)
                .where(DOC.OWNER.eq(userId))
                .fetchInto(DocPO.class);
    }

    /**
     * 根据ID查询文档
     */
    public DocPO queryById(DSLContext dslContext, String id) {
        return dslContext.selectFrom(DOC)
                .where(DOC.ID.eq(id))
                .fetchOneInto(DocPO.class);
    }

    /**
     * 更新文档
     */
    public int update(DSLContext dslContext, DocPO docPO) {
        return dslContext.update(DOC)
                .set(DOC.NAME, docPO.getName())
                .set(DOC.OWNER, docPO.getOwner())
                .set(DOC.TEXT, docPO.getText())
                .set(DOC.TYPE, docPO.getType())
                .where(DOC.ID.eq(docPO.getId()))
                .execute();
    }

    /**
     * 根据ID删除文档
     */
    public int deleteById(DSLContext dslContext, String id) {
        return dslContext.deleteFrom(DOC)
                .where(DOC.ID.eq(id))
                .execute();
    }
}

