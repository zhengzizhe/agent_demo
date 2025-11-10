package com.example.ddd.domain.agent.service.execute.ResearcherService;

import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface ResearcherService {
    TokenStream research(@UserMessage String spec); // spec 可是 JSON/TOON
}