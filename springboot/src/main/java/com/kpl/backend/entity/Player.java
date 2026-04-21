package com.kpl.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "player")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "real_name")
    private String realName;

    private String position;
    private String role;
    private String avatar;

    @Column(name = "birth_date")
    private String birthDate;

    @Column(name = "native_place")
    private String nativePlace;

    @Column(name = "boarding_time")
    private String boardingTime;

    @Column(name = "position_code")
    private Integer positionCode;

    @Column(name = "win_cr", precision = 5, scale = 2)
    private BigDecimal winCr;

    @Column(name = "kda_rank")
    private Integer kdaRank;

    @Column(name = "dmg_min")
    private Integer dmgMin;

    @Column(name = "dmg_min_rank")
    private Integer dmgMinRank;

    @Column(name = "team_dmg_min")
    private Integer teamDmgMin;

    @Column(name = "team_dmg_min_rank")
    private Integer teamDmgMinRank;

    @Column(name = "signature_hero")
    private String signatureHero;

    @Column(precision = 4, scale = 2)
    private BigDecimal kd = BigDecimal.ZERO;

    @Column(name = "mvp_count")
    private Integer mvpCount = 0;

    @Column(name = "games_played")
    private Integer gamesPlayed = 0;

    @Column(name = "is_starter")
    private Boolean isStarter = true;

    @Column(name = "is_fmvp")
    private Boolean isFmvp = false;

    @Column(name = "fmvp_tournament")
    private String fmvpTournament;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PlayerHero> heroPool = new ArrayList<>();
}
