package com.financeiro.backend.features.gamification.dto;

import java.util.List;

public record GamificationProfileResponse(
        long totalXp,
        int level,
        String rank,
        int currentLevelXp,
        int xpPerLevel,
        long nextLevelTotalXp,
        int progressPercentage,
        List<AchievementResponse> achievements,
        List<XpHistoryResponse> recentXpEvents
) {
}
