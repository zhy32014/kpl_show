package com.kpl.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpl.backend.config.DeepSeekProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeepSeekClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;

    public String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt内容不能为空");
        }
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new IllegalStateException("DEEPSEEK_API_KEY未配置");
        }

        String baseUrl = properties.getBaseUrl() == null ? "" : properties.getBaseUrl().trim();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        String url = baseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel());
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("temperature", properties.getTemperature());
        body.put("top_p", properties.getTopP());
        body.put("max_tokens", properties.getMaxTokens());
        body.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            RestTemplate restTemplate = buildRestTemplate(properties.getTimeoutSeconds());
            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
            return extractContent(response);
        } catch (HttpStatusCodeException e) {
            String msg = safeExtractErrorMessage(e.getResponseBodyAsString());
            log.warn("DeepSeek调用失败: status={}, msg={}", e.getStatusCode().value(), msg);
            throw new IllegalStateException("大模型调用失败: " + msg);
        } catch (RestClientException e) {
            log.warn("DeepSeek网络调用异常: {}", e.getMessage());
            throw new IllegalStateException("网络连接失败，请稍后重试");
        }
    }

    private RestTemplate buildRestTemplate(int timeoutSeconds) {
        int timeoutMs = Math.max(1, timeoutSeconds) * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new IllegalStateException("大模型返回为空");
        }
        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new IllegalStateException("大模型返回格式异常");
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstChoice)) {
            throw new IllegalStateException("大模型返回格式异常");
        }
        Object messageObj = firstChoice.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            throw new IllegalStateException("大模型返回格式异常");
        }
        Object contentObj = message.get("content");
        if (!(contentObj instanceof String content) || content.isBlank()) {
            throw new IllegalStateException("大模型返回内容为空");
        }
        return content.trim();
    }

    private String safeExtractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "unknown";
        }
        try {
            Map<?, ?> map = objectMapper.readValue(responseBody, Map.class);
            Object error = map.get("error");
            if (error instanceof Map<?, ?> errorMap) {
                Object message = errorMap.get("message");
                if (message instanceof String m && !m.isBlank()) {
                    return m.trim();
                }
            }
            return "unknown";
        } catch (Exception ignored) {
            return "unknown";
        }
    }
}
