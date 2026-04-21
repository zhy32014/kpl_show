package com.kpl.backend.service;

import com.kpl.backend.config.TeamSyncProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TeamSyncService {

    @Autowired
    private TeamSyncProperties teamSyncProperties;

    private final RestTemplate restTemplate;

    public TeamSyncService() {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(10_000);
        rf.setReadTimeout(15_000);
        this.restTemplate = new RestTemplate(rf);
    }

    public List<Map<String, Object>> fetchTeamList() {
        String apiUrl = cleanText(teamSyncProperties.getApiUrl());
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("未配置队伍信息接口URL(kpl.team-sync.api-url)");
        }

        Object payload;
        try {
            HttpHeaders headers = buildHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);
            ResponseEntity<Object> postResp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Object.class);
            payload = postResp.getBody();
        } catch (RestClientException e) {
            log.warn("POST获取队伍列表失败，尝试GET方式，err={}", e.getMessage());
            payload = restTemplate.getForObject(apiUrl, Object.class);
        }

        List<Map<String, Object>> list = extractTeamList(payload);
        List<Map<String, Object>> normalized = new ArrayList<>();
        for (Map<String, Object> item : list) {
            Map<String, Object> row = new HashMap<>();
            row.put("teamid", asString(item.get("teamid")));
            row.put("team_name", asString(item.get("team_name")));
            String logo = cleanText(asString(item.get("team_logo")));
            if (logo == null || logo.isBlank()) logo = cleanText(asString(item.get("team_logo")));
            if (logo == null || logo.isBlank()) logo = cleanText(asString(item.get("img")));
            row.put("logo", logo);
            normalized.add(row);
        }
        return normalized;
    }

    public Object fetchTeamIntroRaw(String teamId) {
        if (teamId == null || teamId.isBlank()) {
            throw new IllegalArgumentException("teamId不能为空");
        }

        String apiUrl = cleanText(teamSyncProperties.getApiUrl());
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("未配置队伍信息接口URL(kpl.team-sync.api-url)");
        }

        try {
            HttpHeaders headers = buildHeaders();

            Map<String, Object> body = new HashMap<>();
            body.put("teamid", teamId);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Object> postResp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Object.class);
            if (postResp.getBody() != null) return postResp.getBody();
        } catch (RestClientException e) {
            log.warn("POST获取队伍信息失败，尝试GET方式，teamId={}, err={}", teamId, e.getMessage());
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(apiUrl)
                    .queryParam("teamid", teamId)
                    .toUriString();
            return restTemplate.getForObject(url, Object.class);
        } catch (RestClientException e) {
            throw new IllegalStateException("请求队伍信息接口失败: " + e.getMessage(), e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Origin", cleanText(teamSyncProperties.getOrigin()));
        headers.set("Referer", cleanText(teamSyncProperties.getReferer()));
        headers.set("User-Agent", "Mozilla/5.0");
        return headers;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractTeamList(Object payload) {
        if (payload == null) return List.of();

        if (payload instanceof Map<?, ?> map) {
            Object direct = map.get("team_list");
            if (direct instanceof List<?> list) return castMapList(list);

            Object data = map.get("data");
            List<Map<String, Object>> fromData = extractTeamList(data);
            if (!fromData.isEmpty()) return fromData;

            Object result = map.get("result");
            return extractTeamList(result);
        }

        if (payload instanceof List<?> list) {
            return castMapList(list);
        }

        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castMapList(List<?> source) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Object row : source) {
            if (row instanceof Map<?, ?> rowMap) {
                out.add((Map<String, Object>) rowMap);
            }
        }
        return out;
    }

    private String asString(Object val) {
        return val == null ? null : String.valueOf(val);
    }

    private String cleanText(String val) {
        if (val == null) return null;
        return val.replace("`", "").replace("\"", "").trim();
    }
}
