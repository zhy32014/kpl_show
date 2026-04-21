package com.kpl.backend.controller;

import com.kpl.backend.dto.ApiResponse;
import com.kpl.backend.entity.Player;
import com.kpl.backend.entity.Team;
import com.kpl.backend.repository.PlayerRepository;
import com.kpl.backend.repository.TeamRepository;
import com.kpl.backend.service.PlayerSyncService;
import com.kpl.backend.service.TeamSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerSyncService playerSyncService;

    @Autowired
    private TeamSyncService teamSyncService;

    // GET /api/teams — 获取所有战队（按春季赛排名）
    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success(teamRepository.findByOrderBySpringRankAsc()));
    }

    // GET /api/teams/{id} — 获取战队详情
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getTeam(@PathVariable Integer id) {
        return teamRepository.findById(id)
                .map(team -> ResponseEntity.ok(ApiResponse.success(team)))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/teams/{id}/intro — 根据 teamId 从官方接口获取该队伍详情（raw JSON）
    @GetMapping("/{id}/intro")
    public ResponseEntity<ApiResponse<Object>> getTeamIntroRaw(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(teamSyncService.fetchTeamIntroRaw(String.valueOf(id))));
    }

    // GET /api/teams/intro/{teamId} — 使用官方 teamid 获取特定队伍信息（raw JSON）
    @GetMapping("/intro/{teamId}")
    public ResponseEntity<ApiResponse<Object>> getTeamIntroRawByTeamId(@PathVariable String teamId) {
        return ResponseEntity.ok(ApiResponse.success(teamSyncService.fetchTeamIntroRaw(teamId)));
    }

    // GET /api/teams/official/list — 获取官方队伍列表（team_list）
    @GetMapping("/official/list")
    public ResponseEntity<ApiResponse<Object>> getOfficialTeamList() {
        return ResponseEntity.ok(ApiResponse.success(teamSyncService.fetchTeamList()));
    }

    // GET /api/teams/{id}/players — 获取战队选手（来自数据库）
    @GetMapping("/{id}/players")
    public ResponseEntity<ApiResponse<List<Player>>> getTeamPlayers(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(playerRepository.findByTeam_Id(id)));
    }

    // POST /api/teams/{id}/players/sync — 从各战队对应接口同步选手并入库
    @PostMapping("/{id}/players/sync")
    public ResponseEntity<ApiResponse<List<Player>>> syncTeamPlayers(@PathVariable Integer id) {
        try {
            List<Player> synced = playerSyncService.syncTeamPlayers(id);
            return ResponseEntity.ok(ApiResponse.success("同步成功", synced));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error("同步失败: " + e.getMessage()));
        }
    }

    // GET /api/teams/search?q=关键词
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Team>>> search(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(teamRepository.searchByKeyword(q)));
    }

    // GET /api/teams/champion — 当前冠军战队
    @GetMapping("/champion")
    public ResponseEntity<ApiResponse<List<Team>>> getChampion() {
        return ResponseEntity.ok(ApiResponse.success(teamRepository.findByIsChampionTrue()));
    }

    // GET /api/teams/top — 排名前6战队
    @GetMapping("/top")
    public ResponseEntity<ApiResponse<List<Team>>> getTopTeams() {
        return ResponseEntity.ok(ApiResponse.success(teamRepository.findTop6ByOrderBySpringRankAsc()));
    }

    // POST /api/teams — 新增战队（管理员）
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Team>> createTeam(@RequestBody Team team) {
        return ResponseEntity.ok(ApiResponse.success("创建成功", teamRepository.save(team)));
    }

    // PUT /api/teams/{id} — 更新战队（管理员）
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Team>> updateTeam(@PathVariable Integer id, @RequestBody Team updated) {
        return teamRepository.findById(id).map(team -> {
            updated.setId(id);
            return ResponseEntity.ok(ApiResponse.success("更新成功", teamRepository.save(updated)));
        }).orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/teams/{id} — 删除战队（管理员）
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Integer id) {
        teamRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
