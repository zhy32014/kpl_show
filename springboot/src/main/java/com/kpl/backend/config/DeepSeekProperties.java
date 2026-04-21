package com.kpl.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kpl.ai.deepseek")
public class DeepSeekProperties {
    private String baseUrl = "https://api.deepseek.com/v1";
    private String apiKey = "";
    private String model = "deepseek-chat";
    private int timeoutSeconds = 60;
    private double temperature = 0.25;
    private double topP = 0.8;
    private int maxTokens = 1024;
}
