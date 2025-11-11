package com.example.ddd.domain.agent.service.execute.ExecutorService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface ExecutorService {
    /**
     * 执行任务
     * @param spec 规格说明，JSON/TOON 格式字符串
     */
    TokenStream execute(@UserMessage String spec);
}