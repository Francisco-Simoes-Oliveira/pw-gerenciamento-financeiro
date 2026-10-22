package com.financeiro.backend.features.gamification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.gamification.enums.AchievementCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_achievement",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_achievement", columnNames = {"user_id", "achievement_code"}),
        indexes = @Index(name = "idx_user_achievement_user", columnList = "user_id"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "achievement_code", nullable = false, length = 40)
    private AchievementCode achievementCode;

    @Column(nullable = false)
    private LocalDateTime unlockedAt;
}
