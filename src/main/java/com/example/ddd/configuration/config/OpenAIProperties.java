package com.example.ddd.configuration.config;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("openai.api")
@Getter
@Setter
public class OpenAIProperties {

    private String url;
    private String key;
    private String model;


}
