package com.kpl.backend.repository;

import com.kpl.backend.entity.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {
    Page<News> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<News> findByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(String status);
    List<News> findByCategoryAndStatusOrderByCreatedAtDesc(String category, String status);

    @Query("SELECT n FROM News n WHERE n.status = 'published' AND (n.title LIKE %:keyword% OR n.summary LIKE %:keyword%)")
    Page<News> searchByKeyword(String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE News n SET n.views = n.views + 1 WHERE n.id = :id")
    void incrementViews(Long id);
}
