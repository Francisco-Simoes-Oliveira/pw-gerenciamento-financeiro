package com.financeiro.backend.features.gamification.entity;

import com.financeiro.backend.features.gamification.enums.AchievementCode;
import com.financeiro.backend.features.gamification.enums.XpEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "achievement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private AchievementCode code;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false, length = 240)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private XpEventType triggerEvent;

    public static Achievement from(AchievementCode code) {
        return Achievement.builder()
                .code(code)
                .title(code.getTitle())
                .description(code.getDescription())
                .triggerEvent(code.getTrigger())
                .build();
    }
}
