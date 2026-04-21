package com.kpl.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(length = 500)
    private String summary;

    private String author;
    private String category;
    private String tags;
    private String cover;

    @Column(name = "has_video")
    private Boolean hasVideo = false;

    private Integer views = 0;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    private String status = "published";
    private String gradient;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
