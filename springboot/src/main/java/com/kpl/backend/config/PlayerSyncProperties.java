package com.kpl.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "kpl.player-sync")
public class PlayerSyncProperties {
    private Map<String, String> teamApis = new HashMap<>();
}

