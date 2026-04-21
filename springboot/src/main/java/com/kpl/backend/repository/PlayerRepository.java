package com.kpl.backend.repository;

import com.kpl.backend.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    List<Player> findByTeam_Id(Integer teamId);
    Optional<Player> findByTeam_IdAndName(Integer teamId, String name);
}

