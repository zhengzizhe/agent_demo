package com.example.ddd.domain.agent.service.execute.SupervisorService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface SupervisorService {
    TokenStream planAndRoute(@UserMessage String prompt);
}
