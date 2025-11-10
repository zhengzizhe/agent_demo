package com.example.ddd.domain.agent.service.execute.ExecutorService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface ExecutorService {
    TokenStream execute(@UserMessage String spec); // JSON/TOON 字符串
}