package com.kpl.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpl.backend.config.HunyuanProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class HunyuanClient {

    private final HunyuanProperties properties;
    private final ObjectMapper objectMapper;

    public String chat(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Prompt内容不能为空");
        }
        ensureConfigured();

        return chatMessages(List.of(Map.of("role", "user", "content", prompt)), null, null);
    }

    public String chatMessages(List<Map<String, String>> messages, Double temperature, Double topP) {
        ensureConfigured();

        String apiBase = trimRightSlash(properties.getApiBase());
        String path = properties.getPath() == null ? "" : properties.getPath().trim();
        String url = apiBase + path;

        long timestamp = Instant.now().getEpochSecond();
        long expired = timestamp + 24 * 60 * 60;

        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", timestamp);
        payload.put("expired", expired);
        payload.put("messages", messages);
        payload.put("app_id", properties.getAppId());
        payload.put("secret_id", properties.getSecretId());
        payload.put("temperature", temperature != null ? temperature : properties.getTemperature());
        payload.put("top_p", topP != null ? topP : properties.getTopP());

        String authorization = signature(properties.getSecretKey(), url, payload);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", authorization);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            RestTemplate restTemplate = buildRestTemplate(properties.getTimeoutSeconds());
            Map<?, ?> response = restTemplate.postForObject(url, request, Map.class);
            return extractContent(response);
        } catch (HttpStatusCodeException e) {
            String msg = safeExtractErrorMessage(e.getResponseBodyAsString());
            log.warn("混元调用失败: status={}, msg={}", e.getStatusCode().value(), msg);
            throw new IllegalStateException("混元调用失败: " + msg);
        } catch (RestClientException e) {
            log.warn("混元网络调用异常: {}", e.getMessage());
            throw new IllegalStateException("网络连接失败，请稍后重试");
        }
    }

    private void ensureConfigured() {
        if (properties.getAppId() <= 0) {
            throw new IllegalStateException("HUNYUAN_APP_ID未配置");
        }
        if (properties.getSecretId() == null || properties.getSecretId().isBlank()) {
            throw new IllegalStateException("HUNYUAN_SECRET_ID未配置");
        }
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new IllegalStateException("HUNYUAN_SECRET_KEY未配置");
        }
        if (properties.getPath() == null || properties.getPath().isBlank()) {
            throw new IllegalStateException("混元API路径未配置");
        }
    }

    private RestTemplate buildRestTemplate(int timeoutSeconds) {
        int timeoutMs = Math.max(1, timeoutSeconds) * 1000;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);
        return new RestTemplate(factory);
    }

    private String signature(String secretKey, String url, Map<String, Object> payload) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (uri.getPort() != -1) {
                host = host + ":" + uri.getPort();
            }
            String path = uri.getPath();

            Map<String, Object> sorted = new TreeMap<>(payload);
            StringBuilder signStr = new StringBuilder();
            signStr.append(host).append(path).append("?");
            for (Map.Entry<String, Object> entry : sorted.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                signStr.append(key).append("=").append(normalizeValue(value)).append("&");
            }
            signStr.deleteCharAt(signStr.length() - 1);

            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1"));
            byte[] hmac = mac.doFinal(signStr.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new IllegalStateException("混元签名生成失败");
        }
    }

    private String normalizeValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Map<?, ?> || value instanceof List<?>) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                return "";
            }
        }
        if (value instanceof Float || value instanceof Double) {
            return new BigDecimal(String.valueOf(value)).stripTrailingZeros().toPlainString();
        }
        return String.valueOf(value);
    }

    private String extractContent(Map<?, ?> response) {
        if (response == null) {
            throw new IllegalStateException("混元返回为空");
        }
        Object error = response.get("error");
        if (error instanceof Map<?, ?> errorMap) {
            Object message = errorMap.get("message");
            if (message instanceof String m && !m.isBlank()) {
                throw new IllegalStateException(m.trim());
            }
            throw new IllegalStateException("混元返回错误");
        }

        Object choicesObj = response.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new IllegalStateException("混元返回格式异常");
        }

        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstChoice)) {
            throw new IllegalStateException("混元返回格式异常");
        }

        Object msgObj = firstChoice.get("messages");
        if (msgObj == null) {
            msgObj = firstChoice.get("message");
        }
        if (!(msgObj instanceof Map<?, ?> msgMap)) {
            throw new IllegalStateException("混元返回格式异常");
        }

        Object contentObj = msgMap.get("content");
        if (!(contentObj instanceof String content) || content.isBlank()) {
            throw new IllegalStateException("混元返回内容为空");
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

    private String trimRightSlash(String s) {
        if (s == null) return "";
        String r = s.trim();
        while (r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        return r;
    }
}
