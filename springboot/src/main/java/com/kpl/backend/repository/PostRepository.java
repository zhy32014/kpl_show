package com.kpl.backend.repository;

import com.kpl.backend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<Post> findByTopicOrderByCreatedAtDesc(String topic);

    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes + 1 WHERE p.id = :id")
    void incrementLikes(Long id);

    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes - 1 WHERE p.id = :id AND p.likes > 0")
    void decrementLikes(Long id);
}
