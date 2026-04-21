package com.kpl.backend.repository;

import com.kpl.backend.entity.PlayerHero;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerHeroRepository extends JpaRepository<PlayerHero, Integer> {
    void deleteByPlayer_Id(Integer playerId);
}

