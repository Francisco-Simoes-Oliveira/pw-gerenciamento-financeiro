package com.financeiro.backend.features.gamification.dto;

import java.time.LocalDateTime;

import com.financeiro.backend.features.gamification.enums.AchievementCode;

public record AchievementResponse(
        AchievementCode code,
        String title,
        String description,
        boolean unlocked,
        LocalDateTime unlockedAt
) {
}
