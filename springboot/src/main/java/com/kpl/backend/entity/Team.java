package com.kpl.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "short_name", length = 20)
    private String shortName;

    private String city;
    private String stadium;
    private String logo;
    private String color;

    @Column(name = "secondary_color")
    private String secondaryColor;

    @Column(name = "founded_year")
    private Integer foundedYear;

    private String coach;

    @Column(name = "is_champion")
    private Boolean isChampion = false;

    private String championship;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "spring_rank")
    private Integer springRank;

    private Integer wins = 0;
    private Integer losses = 0;

    @Column(name = "win_rate", precision = 5, scale = 2)
    private BigDecimal winRate = BigDecimal.ZERO;

    private Integer points = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Player> roster = new ArrayList<>();

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TeamAchievement> achievements = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
