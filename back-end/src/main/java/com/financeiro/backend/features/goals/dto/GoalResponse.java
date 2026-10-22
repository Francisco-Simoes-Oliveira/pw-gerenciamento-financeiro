package com.financeiro.backend.features.goals.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.financeiro.backend.features.goals.enums.GoalStatus;

public record GoalResponse(
        UUID id,
        UUID walletId,
        String walletName,
        String currency,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        int progressPercentage,
        LocalDate deadline,
        GoalStatus status,
        LocalDateTime createdAt,
        LocalDateTime completedAt
) {
}
