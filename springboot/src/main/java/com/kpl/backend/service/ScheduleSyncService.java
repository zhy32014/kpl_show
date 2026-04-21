package com.kpl.backend.service;

import com.kpl.backend.entity.MatchRecord;
import com.kpl.backend.repository.MatchRecordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ScheduleSyncService {

    @Autowired
    private MatchRecordRepository matchRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kpl.sync.api-url:https://kplshop-op.timi-esports.qq.com/kplow/getScheduleList}")
    private String officialApiUrl;

    @Value("${kpl.sync.mock-fallback:false}")
    private boolean mockFallback;

    @Value("${kpl.sync.season-ids:}")
    private String seasonIdsRaw;

    @Value("${kpl.sync.seasonid-overrides:}")
    private String seasonIdOverridesRaw;

    private static final Pattern SEASON_PATTERN = Pattern.compile("^KPL(\\d{4})S(\\d+).*$");

    /**
     * 自动化同步：每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void autoSync() {
        log.info("触发自动化赛程同步任务...");
        try {
            syncFromOfficial(null);
            log.info("自动化同步成功");
        } catch (Exception e) {
            log.error("自动化同步失败: {}", e.getMessage());
        }
    }

    /**
     * 核心同步逻辑
     */
    @Transactional
    public List<MatchRecord> syncFromOfficial(String seasonId) {
        List<String> seasonIdList = new ArrayList<>();
        if (seasonId != null && !seasonId.isBlank()) {
            seasonIdList.add(seasonId);
        } else {
            seasonIdList.addAll(getConfiguredSeasonIds());
        }

        if (seasonIdList.isEmpty()) {
            log.info("开始从官网获取最新赛程数据（不指定 seasonid），接口: {}", officialApiUrl);
            List<Map<String, Object>> rawData = fetchOfficialRawData(null);
            List<MatchRecord> list = parseOfficialData(rawData);
            persist(list);
            return list;
        }

        log.info("开始从官网获取赛程数据，接口: {}，seasonIds={}", officialApiUrl, seasonIdList);
        Map<String, String> overrides = getSeasonIdOverrides();
        Map<String, MatchRecord> byId = new HashMap<>();
        for (String sid : seasonIdList) {
            List<String> candidates = resolveSeasonIdCandidates(sid, overrides);
            boolean foundAny = false;
            for (String candidate : candidates) {
                List<Map<String, Object>> raw = fetchOfficialRawData(candidate);
                List<MatchRecord> parsed = parseOfficialData(raw);
                if (!parsed.isEmpty()) {
                    foundAny = true;
                    for (MatchRecord m : parsed) {
                        if (m.getId() != null) byId.put(m.getId(), m);
                    }
                    break;
                }
            }
            if (!foundAny) {
                log.warn("未获取到该赛季的赛程数据：requestedSeasonId={} candidates={}", sid, candidates);
            }
        }

        List<MatchRecord> merged = new ArrayList<>(byId.values());
        persist(merged);
        return merged;
    }

    private void persist(List<MatchRecord> list) {
        if (!list.isEmpty()) {
            matchRepository.saveAll(list);
            log.info("成功同步 {} 条比赛记录到数据库", list.size());
        } else {
            log.warn("官网接口未返回可解析赛程，未写入数据库。");
        }
    }

    /**
     * 解析官网返回的赛程 JSON 数据
     */
    private List<MatchRecord> parseOfficialData(List<Map<String, Object>> rawData) {
        List<MatchRecord> list = new ArrayList<>();
        if (rawData == null) return list;

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (Map<String, Object> item : rawData) {
            try {
                MatchRecord match = new MatchRecord();
                
                // 1. 基础信息映射
                String scheduleId = asString(item.get("scheduleid"));
                String seasonId = asString(item.get("seasonid"));
                String stageName = asString(item.get("stage_name"));
                String locationName = asString(item.get("location_name"));
                Integer boTotal = asInteger(item.get("bo_total"));

                if (scheduleId == null || scheduleId.isBlank()) {
                    // 主键必须存在，缺失时跳过，避免污染数据
                    log.warn("跳过一条缺少 scheduleid 的赛程数据: {}", item);
                    continue;
                }
                if (asString(item.get("team_a_id")) == null || asString(item.get("team_b_id")) == null) {
                    log.warn("跳过一条缺少战队 ID 的赛程数据, scheduleId={}", scheduleId);
                    continue;
                }
                if (asLong(item.get("start_timestamp")) == null) {
                    log.warn("跳过一条缺少开赛时间戳的赛程数据, scheduleId={}", scheduleId);
                    continue;
                }

                match.setId(scheduleId);
                match.setTournament(buildTournamentName(seasonId));
                match.setStage(stageName);
                match.setVenue(locationName);
                match.setFormat(boTotal != null ? "BO" + boTotal : null);

                // 2. 时间解析 (Unix 时间戳)
                Long timestamp = asLong(item.get("start_timestamp"));
                if (timestamp != null) {
                    Instant instant = Instant.ofEpochSecond(timestamp);
                    ZoneId zone = ZoneId.of("Asia/Shanghai");
                    match.setMatchDate(instant.atZone(zone).toLocalDate());
                    match.setMatchTime(instant.atZone(zone).toLocalTime().format(timeFormatter));
                }

                // 3. 状态映射 (官网 4 通常表示已结束)
                Integer officialStatus = asInteger(item.get("schedule_status"));
                if (officialStatus == 4) {
                    match.setStatus("finished");
                } else if (officialStatus == 2) {
                    match.setStatus("live");
                } else if (officialStatus == 1) {
                    match.setStatus("upcoming");
                } else {
                    match.setStatus("upcoming");
                }

                // 4. 战队信息
                match.setTeamAId(asString(item.get("team_a_id")));
                match.setTeamAName(asString(item.get("team_a_name")));
                match.setTeamAScore(asInteger(item.get("team_a_score")));
                match.setTeamALogo(cleanLogoUrl(asString(item.get("team_a_logo"))));

                match.setTeamBId(asString(item.get("team_b_id")));
                match.setTeamBName(asString(item.get("team_b_name")));
                match.setTeamBScore(asInteger(item.get("team_b_score")));
                match.setTeamBLogo(cleanLogoUrl(asString(item.get("team_b_logo"))));

                if (match.getTeamAScore() == null) match.setTeamAScore(0);
                if (match.getTeamBScore() == null) match.setTeamBScore(0);

                // 5. 结果判断
                if ("finished".equals(match.getStatus())) {
                    if (match.getTeamAScore() > match.getTeamBScore()) {
                        match.setResult(match.getTeamAName() + "胜");
                    } else if (match.getTeamAScore() < match.getTeamBScore()) {
                        match.setResult(match.getTeamBName() + "胜");
                    }
                }

                list.add(match);
            } catch (Exception e) {
                log.error("解析单条赛程数据失败: {}, 数据内容: {}", e.getMessage(), item);
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchOfficialRawData(String seasonId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            Map<String, Object> body = new HashMap<>();
            if (seasonId != null && !seasonId.isBlank()) {
                body.put("seasonid", seasonId);
            }
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Object> postResp = restTemplate.exchange(officialApiUrl, HttpMethod.POST, entity, Object.class);
            List<Map<String, Object>> postNormalized = normalizePayload(postResp.getBody());
            if (!postNormalized.isEmpty()) {
                return postNormalized;
            }

            if (isRecognizedContainer(postResp.getBody())) {
                return List.of();
            }

            log.warn("官网接口返回结构无法识别，payload类型(POST): {}", postResp.getBody() == null ? "null" : postResp.getBody().getClass().getName());
            throw new IllegalStateException("官网接口返回结构无法识别，未找到赛程数组。请提供接口完整响应样例。");
        } catch (RestClientException e) {
            if (mockFallback) {
                log.warn("请求官网接口失败，使用本地示例数据兜底: {}", e.getMessage());
                return getMockRawData();
            }
            throw new IllegalStateException("请求官网接口失败: " + e.getMessage(), e);
        }
    }

    private List<String> getConfiguredSeasonIds() {
        if (seasonIdsRaw == null) return List.of();
        String raw = seasonIdsRaw.trim();
        if (raw.isBlank()) return List.of();
        raw = raw.replace("[", "").replace("]", "");
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<String, String> getSeasonIdOverrides() {
        if (seasonIdOverridesRaw == null) return Map.of();
        String raw = seasonIdOverridesRaw.trim();
        if (raw.isBlank()) return Map.of();

        Map<String, String> map = new HashMap<>();
        for (String pair : raw.split(";")) {
            String p = pair == null ? "" : pair.trim();
            if (p.isBlank()) continue;
            int idx = p.indexOf('=');
            if (idx <= 0 || idx >= p.length() - 1) continue;
            String k = p.substring(0, idx).trim();
            String v = p.substring(idx + 1).trim();
            if (!k.isBlank() && !v.isBlank()) map.put(k, v);
        }
        return map;
    }

    private List<String> resolveSeasonIdCandidates(String requested, Map<String, String> overrides) {
        if (requested == null || requested.isBlank()) return List.of();

        String override = overrides.get(requested);
        if (override != null && !override.isBlank()) return List.of(override);

        List<String> candidates = new ArrayList<>();
        candidates.add(requested);

        Matcher m = SEASON_PATTERN.matcher(requested);
        if (m.matches()) {
            int year = Integer.parseInt(m.group(1));
            String seasonNo = m.group(2);
            if (year <= 2018) {
                String base = "KPL" + year;
                candidates.add(base);
                candidates.add(base + seasonNo);
                candidates.add(base + (("1".equals(seasonNo)) ? "Spring" : "Autumn"));
            }
        }

        return candidates.stream().distinct().collect(Collectors.toList());
    }

    private boolean isRecognizedContainer(Object payload) {
        if (!(payload instanceof Map<?, ?> map)) return false;
        return map.containsKey("data") || map.containsKey("list") || map.containsKey("schedule_list") || map.containsKey("result");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizePayload(Object payload) {
        if (payload == null) return List.of();

        if (payload instanceof List<?> payloadList) {
            return castMapList(payloadList);
        }

        if (payload instanceof Map<?, ?> payloadMap) {
            Object data = payloadMap.get("data");
            if (data instanceof List<?> listData) return castMapList(listData);
            if (data instanceof Map<?, ?> dataMap) {
                Object dataList = dataMap.get("list");
                if (dataList instanceof List<?> listData) return castMapList(listData);
                Object dataScheduleList = dataMap.get("schedule_list");
                if (dataScheduleList instanceof List<?> listData) return castMapList(listData);
            }

            Object list = payloadMap.get("list");
            if (list instanceof List<?> listData) return castMapList(listData);

            Object scheduleList = payloadMap.get("schedule_list");
            if (scheduleList instanceof List<?> listData) return castMapList(listData);

            Object result = payloadMap.get("result");
            if (result instanceof Map<?, ?> resultMap) {
                Object resultList = resultMap.get("list");
                if (resultList instanceof List<?> listData) return castMapList(listData);
                Object resultScheduleList = resultMap.get("schedule_list");
                if (resultScheduleList instanceof List<?> listData) return castMapList(listData);
            }
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castMapList(List<?> source) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object row : source) {
            if (row instanceof Map<?, ?> rowMap) {
                result.add((Map<String, Object>) rowMap);
            }
        }
        return result;
    }

    private String cleanLogoUrl(String url) {
        if (url == null) return null;
        return url.replace("`", "").trim();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Long asLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String buildTournamentName(String seasonId) {
        if (seasonId == null || seasonId.isBlank()) {
            return "KPL赛程";
        }
        Matcher matcher = SEASON_PATTERN.matcher(seasonId);
        if (!matcher.matches()) {
            return seasonId;
        }
        String year = matcher.group(1);
        String seasonNo = matcher.group(2);
        String seasonName = "1".equals(seasonNo) ? "春季赛" : "夏季赛";
        return year + "年KPL" + seasonName;
    }

    private List<Map<String, Object>> getMockRawData() {
        List<Map<String, Object>> rawData = new ArrayList<>();

        Map<String, Object> match1 = new HashMap<>();
        match1.put("scheduleid", "KPL2026S1M1W1D1");
        match1.put("seasonid", "KPL2026S1");
        match1.put("stageid", "cgs1");
        match1.put("stage_name", "常规赛第一轮");
        match1.put("start_timestamp", "1768370400");
        match1.put("team_a_id", "KPL2026S1_estar");
        match1.put("team_a_name", "武汉eStarPro");
        match1.put("team_a_group", "B");
        match1.put("team_a_score", 2);
        match1.put("team_a_logo", " `https://smhtv-pic.tga.qq.com/a9b49ca42d0e04ca85bfc070ba548cc8.png` ");
        match1.put("team_b_id", "KPL2026S1_vg");
        match1.put("team_b_name", "北京JDG");
        match1.put("team_b_group", "B");
        match1.put("team_b_score", 3);
        match1.put("team_b_logo", " `https://smhtv-pic.tga.qq.com/bc18df69dd91643409c09a3503737cb3.png` ");
        match1.put("bo_total", 5);
        match1.put("schedule_status", 4);
        match1.put("competition_format", "单循环BO5");
        match1.put("location_name", "上海");
        
        rawData.add(match1);
        return rawData;
    }
}
