package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.entity.*;
import com.kpl.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理后台控制器 — 所有接口需要 ADMIN 角色
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRecordRepository matchRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private PostRepository postRepository;

    // ====== 仪表盘统计 ======
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTeams", teamRepository.count());
        stats.put("totalMatches", matchRepository.count());
        stats.put("totalNews", newsRepository.count());
        stats.put("totalPosts", postRepository.count());
        stats.put("ongoingMatches", matchRepository.findByStatusOrderByMatchDateDesc("ongoing").size());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ====== 用户管理 ======
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userRepository.findAll(PageRequest.of(page, size)).getContent()));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<String>> updateRole(
            @PathVariable Long id, @RequestParam String role) {
        userRepository.findById(id).ifPresent(user -> {
            user.setRole(role);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success("角色已更新", null));
    }

    @PutMapping("/users/{id}/enabled")
    public ResponseEntity<ApiResponse<String>> toggleEnabled(
            @PathVariable Long id, @RequestParam Boolean enabled) {
        userRepository.findById(id).ifPresent(user -> {
            user.setEnabled(enabled);
            userRepository.save(user);
        });
        return ResponseEntity.ok(ApiResponse.success(enabled ? "账号已启用" : "账号已禁用", null));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    // ====== 比赛管理：直接修改比分/状态 ======
    @PatchMapping("/matches/{id}/score")
    public ResponseEntity<ApiResponse<MatchRecord>> updateScore(
            @PathVariable String id,
            @RequestParam Integer scoreA,
            @RequestParam Integer scoreB,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String result) {
        return matchRepository.findById(id).map(match -> {
            match.setTeamAScore(scoreA);
            match.setTeamBScore(scoreB);
            if (status != null) match.setStatus(status);
            if (result != null) match.setResult(result);
            return ResponseEntity.ok(ApiResponse.success("比分已更新", matchRepository.save(match)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/matches/{id}/replay")
    public ResponseEntity<ApiResponse<MatchRecord>> updateReplay(
            @PathVariable String id,
            @RequestParam String replayUrl) {
        return matchRepository.findById(id).map(match -> {
            match.setReplayUrl(replayUrl);
            return ResponseEntity.ok(ApiResponse.success("回放链接已更新", matchRepository.save(match)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ====== 内容审核：帖子管理 ======
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<Post>>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).getContent()));
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("帖子已删除", null));
    }
}
