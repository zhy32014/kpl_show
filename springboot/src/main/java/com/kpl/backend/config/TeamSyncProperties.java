package com.kpl.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kpl.team-sync")
public class TeamSyncProperties {
    private String apiUrl = "https://kplshop-op.timi-esports.qq.com/kplow/getTeamsIntro";
    private String origin = "https://kpl.qq.com";
    private String referer = "https://kpl.qq.com/";
}
