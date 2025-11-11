package com.example.ddd.configuration.config;

import dev.langchain4j.service.TokenStream;

public interface ChatApi {
    String chat(String userMessage);

    TokenStream stream(String userMessage);
}