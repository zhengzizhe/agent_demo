package com.example.ddd.configuration.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "openai.api")
@Getter
@Setter
public class OpenAIProperties {

    private String url;
    private String key;
    private String model;

}
