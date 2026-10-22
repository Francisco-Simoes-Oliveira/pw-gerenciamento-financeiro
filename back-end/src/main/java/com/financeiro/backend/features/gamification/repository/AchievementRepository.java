package com.financeiro.backend.features.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.backend.features.gamification.entity.Achievement;
import com.financeiro.backend.features.gamification.enums.AchievementCode;

public interface AchievementRepository extends JpaRepository<Achievement, AchievementCode> {
}
