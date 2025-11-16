package com.example.ddd.domain.agent.service.armory;

import dev.langchain4j.service.TokenStream;

public interface AiService {
    /**
     * 聊天方法（使用配置的systemPrompt）
     */
    TokenStream chat(String user);

    /**
     * 聊天方法（带额外的systemPrompt，会与配置的systemPrompt拼接）
     * 
     * @param user 用户输入
     * @param extraSystemPrompt 额外的系统提示词（会与配置的systemPrompt拼接）
     */
    TokenStream chat(String user, String extraSystemPrompt);
}
