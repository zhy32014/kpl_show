package com.kpl.backend.service;

import com.kpl.backend.entity.Player;
import com.kpl.backend.entity.PlayerHero;
import com.kpl.backend.entity.Team;
import com.kpl.backend.config.PlayerSyncProperties;
import com.kpl.backend.repository.PlayerHeroRepository;
import com.kpl.backend.repository.PlayerRepository;
import com.kpl.backend.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PlayerSyncService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerHeroRepository playerHeroRepository;

    @Autowired
    private PlayerSyncProperties playerSyncProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    @Transactional
    public List<Player> syncTeamPlayers(Integer teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalStateException("战队不存在: " + teamId));

        String apiUrl = playerSyncProperties.getTeamApis().get(String.valueOf(teamId));
        if (apiUrl == null || apiUrl.isBlank()) {
            throw new IllegalStateException("未配置该战队的选手接口URL，teamId=" + teamId);
        }

        List<Map<String, Object>> rawPlayers = fetchRawPlayers(apiUrl);
        if (rawPlayers.isEmpty()) {
            throw new IllegalStateException("接口未返回可解析的选手列表，teamId=" + teamId);
        }

        List<Player> saved = new ArrayList<>();
        for (Map<String, Object> row : rawPlayers) {
            String shortName = asString(row.get("player_name_short"));
            if (shortName == null || shortName.isBlank()) continue;

            Player player = playerRepository.findByTeam_IdAndName(teamId, shortName).orElseGet(Player::new);
            player.setTeam(team);
            player.setName(shortName);
            player.setRealName(asString(row.get("player_name_real")));
            player.setPosition(asString(row.get("position_cn")));
            player.setPositionCode(asInteger(row.get("position")));
            player.setAvatar(cleanUrl(asString(row.get("avatar")), asString(row.get("photo"))));
            player.setBirthDate(asString(row.get("birth_date")));
            player.setNativePlace(asString(row.get("native_place")));
            player.setBoardingTime(asString(row.get("boarding_time")));

            Integer matchCount = asInteger(row.get("Matchcount"));
            if (matchCount != null) player.setGamesPlayed(matchCount);

            BigDecimal kda = asBigDecimal(row.get("KDA"));
            if (kda != null) player.setKd(kda);
            player.setKdaRank(asInteger(row.get("KDARank")));

            player.setWinCr(asBigDecimal(row.get("Wincr")));
            player.setDmgMin(asInteger(row.get("DMGMin")));
            player.setDmgMinRank(asInteger(row.get("DMGMinRank")));
            player.setTeamDmgMin(asInteger(row.get("TDMGMin")));
            player.setTeamDmgMinRank(asInteger(row.get("TDMGMinRank")));

            Player savedPlayer = playerRepository.save(player);

            playerHeroRepository.deleteByPlayer_Id(savedPlayer.getId());
            List<PlayerHero> heroPool = buildHeroPool(savedPlayer, row.get("HeroPool"));
            savedPlayer.setHeroPool(heroPool);
            saved.add(playerRepository.save(savedPlayer));
        }

        return saved;
    }

    private List<PlayerHero> buildHeroPool(Player player, Object heroPoolObj) {
        List<PlayerHero> result = new ArrayList<>();
        if (!(heroPoolObj instanceof List<?> list)) return result;
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> heroMap)) continue;
            String heroName = asString(heroMap.get("hero_name"));
            if (heroName == null || heroName.isBlank()) continue;
            PlayerHero hero = new PlayerHero();
            hero.setPlayer(player);
            hero.setHeroName(heroName);
            result.add(hero);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchRawPlayers(String apiUrl) {
        try {
            Object payload = restTemplate.getForObject(apiUrl, Object.class);
            List<Map<String, Object>> normalized = normalizePayload(payload);
            if (!normalized.isEmpty()) return normalized;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(new HashMap<>(), headers);
            ResponseEntity<Object> postResp = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Object.class);
            return normalizePayload(postResp.getBody());
        } catch (RestClientException e) {
            throw new IllegalStateException("请求选手接口失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizePayload(Object payload) {
        if (payload == null) return List.of();

        if (payload instanceof List<?> list) {
            return castMapList(list);
        }

        if (payload instanceof Map<?, ?> map) {
            Object data = map.get("data");
            List<Map<String, Object>> fromData = normalizePayload(data);
            if (!fromData.isEmpty()) return fromData;

            Object list = map.get("list");
            if (list instanceof List<?> listData) return castMapList(listData);

            Object players = map.get("players");
            if (players instanceof List<?> listData) return castMapList(listData);

            Object playerList = map.get("player_list");
            if (playerList instanceof List<?> listData) return castMapList(listData);

            Object result = map.get("result");
            return normalizePayload(result);
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

    private String cleanUrl(String primary, String fallback) {
        String url = (primary == null || primary.isBlank()) ? fallback : primary;
        if (url == null) return null;
        return url.replace("`", "").trim();
    }

    private String asString(Object val) {
        return val == null ? null : String.valueOf(val);
    }

    private Integer asInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(val));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private BigDecimal asBigDecimal(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
