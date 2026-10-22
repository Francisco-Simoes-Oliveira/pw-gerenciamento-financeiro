package com.financeiro.backend.features.gamification.service;

import org.springframework.stereotype.Component;

@Component
public class LevelCalculator {
    public static final int XP_PER_LEVEL = 200;

    public LevelProgress calculate(long totalXp) {
        long safeXp = Math.max(0, totalXp);
        int level = Math.toIntExact(Math.min(Integer.MAX_VALUE, safeXp / XP_PER_LEVEL + 1));
        int currentLevelXp = (int) (safeXp % XP_PER_LEVEL);
        int percentage = currentLevelXp * 100 / XP_PER_LEVEL;
        return new LevelProgress(level, rankFor(level), currentLevelXp, XP_PER_LEVEL,
                (long) level * XP_PER_LEVEL, percentage);
    }

    private String rankFor(int level) {
        if (level >= 5) return "Mestre Financeiro";
        if (level == 4) return "Estrategista Financeiro";
        if (level == 3) return "Planejador Financeiro";
        if (level == 2) return "Organizador Financeiro";
        return "Explorador Financeiro";
    }

    public record LevelProgress(
            int level,
            String rank,
            int currentLevelXp,
            int xpPerLevel,
            long nextLevelTotalXp,
            int progressPercentage
    ) {
    }
}
