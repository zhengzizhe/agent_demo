package com.example.ddd.domain.agent.service.execute.CriticService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface CriticService {
    /**
     * 评审任务
     * @param prompt 提示词，包含草稿和需求
     */
    TokenStream review(@UserMessage String prompt);
}