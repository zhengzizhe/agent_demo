package com.example.ddd.interfaces.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocView {
    private String id;
    private String owner;
    private String name;
    private String text; // 文档内容（可选，列表接口可能不返回）
}
