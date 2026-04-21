package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.entity.*;
import com.kpl.backend.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    // GET /api/community/posts?page=0&size=20
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<Post>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                postRepository.findAllByOrderByCreatedAtDesc(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    // POST /api/community/posts — 发帖（需登录）
    @PostMapping("/posts")
    @Transactional
    public ResponseEntity<ApiResponse<Post>> createPost(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Post post = new Post();
        post.setUser(user);
        post.setContent(body.get("content"));
        post.setTopic(body.get("topic"));
        return ResponseEntity.ok(ApiResponse.success("发布成功", postRepository.save(post)));
    }

    // GET /api/community/posts/{id}/comments
    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<ApiResponse<List<Comment>>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(commentRepository.findByPostIdOrderByCreatedAtAsc(id)));
    }

    // POST /api/community/posts/{id}/comments — 评论（需登录）
    @PostMapping("/posts/{id}/comments")
    @Transactional
    public ResponseEntity<ApiResponse<Comment>> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Post post = postRepository.findById(id).orElseThrow();

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(body.get("content"));
        commentRepository.save(comment);

        post.setComments(post.getComments() + 1);
        postRepository.save(post);

        return ResponseEntity.ok(ApiResponse.success("评论成功", comment));
    }

    // POST /api/community/posts/{id}/like — 点赞（需登录）
    @PostMapping("/posts/{id}/like")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> likePost(@PathVariable Long id) {
        postRepository.incrementLikes(id);
        return ResponseEntity.ok(ApiResponse.success("点赞成功", null));
    }

    // DELETE /api/community/posts/{id} — 删除帖子（本人或管理员）
    @DeleteMapping("/posts/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Post post = postRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        if (!post.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            return ResponseEntity.status(403).body(ApiResponse.error("无权限删除"));
        }
        postRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
