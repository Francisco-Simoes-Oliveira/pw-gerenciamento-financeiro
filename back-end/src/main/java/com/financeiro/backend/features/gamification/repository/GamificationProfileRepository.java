package com.financeiro.backend.features.gamification.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.backend.features.gamification.entity.GamificationProfile;

public interface GamificationProfileRepository extends JpaRepository<GamificationProfile, UUID> {
    Optional<GamificationProfile> findByUserId(UUID userId);
}
