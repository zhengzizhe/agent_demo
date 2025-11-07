package com.example.ddd.configuration.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Factory
@Singleton
@Slf4j
public class OpenAIConfig {

//    @Singleton
//    public OpenAiChatModel openAiChatModel(OpenAIProperties openAIProperties
//    ) {
//        return OpenAiChatModel.builder()
//                .apiKey(openAIProperties.getKey())
//                .baseUrl(openAIProperties.getUrl())
//                .logRequests(true)
//                .logResponses(true)
//                .logger(log)
//                .modelName(openAIProperties.getModel())
//                .temperature(0.7)
//                .maxTokens(2000)
//                .maxRetries(3)
//                .timeout(Duration.ofSeconds(20))
//                .build();
//    }
}
