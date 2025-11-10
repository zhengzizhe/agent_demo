package com.example.ddd.domain.agent.service.execute.CriticService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface CriticService {
    TokenStream review(@UserMessage String prompt); // prompt包含draft和requirements
}