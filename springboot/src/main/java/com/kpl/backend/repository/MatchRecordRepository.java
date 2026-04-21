package com.kpl.backend.repository;

import com.kpl.backend.entity.MatchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MatchRecordRepository extends JpaRepository<MatchRecord, String> {
    List<MatchRecord> findByStatusOrderByMatchDateDesc(String status);
    List<MatchRecord> findByTournamentOrderByMatchDateDesc(String tournament);
    List<MatchRecord> findByStageOrderByMatchDateDesc(String stage);

    @Query("SELECT m FROM MatchRecord m WHERE m.teamAId = :teamId OR m.teamBId = :teamId ORDER BY m.matchDate DESC")
    List<MatchRecord> findByTeamId(@Param("teamId") String teamId);

    @Query("SELECT m FROM MatchRecord m ORDER BY m.matchDate DESC")
    List<MatchRecord> findRecentMatches(org.springframework.data.domain.Pageable pageable);

    List<MatchRecord> findByStageContainingOrderByMatchDateDesc(String stage);
}
