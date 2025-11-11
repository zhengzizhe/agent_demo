package com.example.ddd.domain.agent.service.execute.ResearcherService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface ResearcherService {
    /**
     * 研究任务
     * @param spec 规格说明，可以是 JSON/TOON 格式
     */
    TokenStream research(@UserMessage String spec);
}