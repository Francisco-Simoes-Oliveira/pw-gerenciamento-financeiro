package com.financeiro.backend.features.gamification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.backend.features.gamification.entity.UserAchievement;
import com.financeiro.backend.features.gamification.enums.AchievementCode;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, UUID> {
    List<UserAchievement> findByUserIdOrderByUnlockedAtDesc(UUID userId);
    boolean existsByUserIdAndAchievementCode(UUID userId, AchievementCode achievementCode);
}
