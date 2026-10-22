package com.financeiro.backend.features.gamification.dto;

import java.time.LocalDateTime;

import com.financeiro.backend.features.gamification.enums.XpEventType;

public record XpHistoryResponse(
        XpEventType type,
        String label,
        int xp,
        LocalDateTime occurredAt
) {
}
