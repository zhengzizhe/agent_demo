package com.example.ddd.configuration.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.service.TokenStream;

import java.util.List;

public interface ChatApi {
    String chat(String userMessage);

    TokenStream stream(String userMessage);
}