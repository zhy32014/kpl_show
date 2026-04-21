package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.entity.News;
import com.kpl.backend.repository.NewsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsRepository newsRepository;

    // GET /api/news?page=0&size=10
    @GetMapping
    public ResponseEntity<ApiResponse<Page<News>>> getNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(newsRepository.findByStatusOrderByCreatedAtDesc(
                "published", PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    // GET /api/news/{id}
    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<News>> getNewsDetail(@PathVariable Long id) {
        return newsRepository.findById(id).map(news -> {
            newsRepository.incrementViews(id);
            return ResponseEntity.ok(ApiResponse.success(news));
        }).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/news/featured — 头条新闻
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<News>>> getFeatured() {
        return ResponseEntity.ok(ApiResponse.success(
                newsRepository.findByIsFeaturedTrueAndStatusOrderByCreatedAtDesc("published")));
    }

    // GET /api/news/category/{category}
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<News>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                newsRepository.findByCategoryAndStatusOrderByCreatedAtDesc(category, "published")));
    }

    // GET /api/news/search?q=关键词
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<News>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                newsRepository.searchByKeyword(q, PageRequest.of(page, size))));
    }

    // POST /api/news — 管理员发布
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<News>> createNews(@RequestBody News news) {
        return ResponseEntity.ok(ApiResponse.success("发布成功", newsRepository.save(news)));
    }

    // PUT /api/news/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<News>> updateNews(@PathVariable Long id, @RequestBody News updated) {
        updated.setId(id);
        return ResponseEntity.ok(ApiResponse.success("更新成功", newsRepository.save(updated)));
    }

    // DELETE /api/news/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteNews(@PathVariable Long id) {
        newsRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
