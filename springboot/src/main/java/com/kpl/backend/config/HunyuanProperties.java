package com.kpl.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kpl.ai.hunyuan")
public class HunyuanProperties {
    private String apiBase = "https://hunyuan.cloud.tencent.com";
    private String path = "/hyllm/v1/chat/completions";
    private long appId = 0;
    private String secretId;
    private String secretKey;
    private int timeoutSeconds = 60;
    private double temperature = 0.7;
    private double topP = 1.0;
}
