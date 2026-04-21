package com.kpl.backend.repository;

import com.kpl.backend.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Integer> {
    List<Team> findByOrderBySpringRankAsc();
    List<Team> findByIsChampionTrue();
    Optional<Team> findByName(String name);

    @Query("SELECT t FROM Team t WHERE t.name LIKE %:keyword% OR t.shortName LIKE %:keyword% OR t.city LIKE %:keyword%")
    List<Team> searchByKeyword(String keyword);

    List<Team> findTop6ByOrderBySpringRankAsc();
}
