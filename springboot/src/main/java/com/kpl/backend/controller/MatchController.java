package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.entity.MatchRecord;
import com.kpl.backend.repository.MatchRecordRepository;
import com.kpl.backend.service.ScheduleSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matches")
public class MatchController {

    @Autowired
    private MatchRecordRepository matchRepository;

    @Autowired
    private ScheduleSyncService syncService;

    // 手动触发从官网同步数据
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<List<MatchRecord>>> syncOfficialData(
            @RequestParam(required = false) String seasonId,
            @RequestParam(required = false) String seasonIds) {
        try {
            if (seasonIds != null && !seasonIds.isBlank()) {
                List<MatchRecord> merged = new java.util.ArrayList<>();
                for (String sid : seasonIds.split(",")) {
                    String trimmed = sid == null ? null : sid.trim();
                    if (trimmed == null || trimmed.isBlank()) continue;
                    merged.addAll(syncService.syncFromOfficial(trimmed));
                }
                return ResponseEntity.ok(ApiResponse.success("同步成功，已从官网抓取最新数据", merged));
            }

            List<MatchRecord> synced = syncService.syncFromOfficial(seasonId);
            return ResponseEntity.ok(ApiResponse.success("同步成功，已从官网抓取最新数据", synced));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("同步失败: " + e.getMessage()));
        }
    }

    // GET /api/matches — 所有比赛
    @GetMapping
    public ResponseEntity<ApiResponse<List<MatchRecord>>> getAllMatches() {
        return ResponseEntity.ok(ApiResponse.success(matchRepository.findAll()));
    }

    // GET /api/matches/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MatchRecord>> getMatch(@PathVariable String id) {
        return matchRepository.findById(id)
                .map(m -> ResponseEntity.ok(ApiResponse.success(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/matches/status/{status} — upcoming/ongoing/finished
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<MatchRecord>>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(ApiResponse.success(
                matchRepository.findByStatusOrderByMatchDateDesc(status)));
    }

    // GET /api/matches/team/{teamId} — 某战队所有比赛
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<MatchRecord>>> getByTeam(@PathVariable String teamId) {
        return ResponseEntity.ok(ApiResponse.success(matchRepository.findByTeamId(teamId)));
    }

    // GET /api/matches/recent?size=10 — 最近比赛
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<MatchRecord>>> getRecent(
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                matchRepository.findRecentMatches(PageRequest.of(0, size))));
    }

    // GET /api/matches/finals — 历届总决赛
    @GetMapping("/finals")
    public ResponseEntity<ApiResponse<List<MatchRecord>>> getFinals() {
        return ResponseEntity.ok(ApiResponse.success(
                matchRepository.findByStageContainingOrderByMatchDateDesc("总决赛")));
    }

    // POST /api/matches — 管理员新增比赛
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchRecord>> createMatch(@RequestBody MatchRecord match) {
        return ResponseEntity.ok(ApiResponse.success("创建成功", matchRepository.save(match)));
    }

    // PUT /api/matches/{id} — 管理员更新比赛
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MatchRecord>> updateMatch(
            @PathVariable String id, @RequestBody MatchRecord updated) {
        updated.setId(id);
        return ResponseEntity.ok(ApiResponse.success("更新成功", matchRepository.save(updated)));
    }

    // DELETE /api/matches/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMatch(@PathVariable String id) {
        matchRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
