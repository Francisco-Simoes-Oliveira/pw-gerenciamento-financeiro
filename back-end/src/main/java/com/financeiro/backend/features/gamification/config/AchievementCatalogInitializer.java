package com.financeiro.backend.features.gamification.config;

import java.util.Arrays;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.features.gamification.entity.Achievement;
import com.financeiro.backend.features.gamification.enums.AchievementCode;
import com.financeiro.backend.features.gamification.repository.AchievementRepository;

@Component
public class AchievementCatalogInitializer implements ApplicationRunner {
    private final AchievementRepository achievements;

    public AchievementCatalogInitializer(AchievementRepository achievements) {
        this.achievements = achievements;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Arrays.stream(AchievementCode.values())
                .filter(code -> !achievements.existsById(code))
                .map(Achievement::from)
                .forEach(achievements::save);
    }
}
