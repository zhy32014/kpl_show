package com.kpl.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "match_record")
public class MatchRecord {
    @Id
    private String id;

    @Column(nullable = false)
    private String tournament;

    private String stage;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "match_time", length = 10)
    private String matchTime;

    private String venue;

    @Column(nullable = false, length = 20)
    private String status = "upcoming";

    @Column(name = "team_a_id", nullable = false)
    private String teamAId;

    @Column(name = "team_a_name")
    private String teamAName;

    @Column(name = "team_a_score")
    private Integer teamAScore = 0;

    @Column(name = "team_a_logo")
    private String teamALogo;

    @Column(name = "team_b_id", nullable = false)
    private String teamBId;

    @Column(name = "team_b_name")
    private String teamBName;

    @Column(name = "team_b_score")
    private Integer teamBScore = 0;

    @Column(name = "team_b_logo")
    private String teamBLogo;

    @Column(name = "replay_url")
    private String replayUrl;

    private String result;
    private String format;
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
