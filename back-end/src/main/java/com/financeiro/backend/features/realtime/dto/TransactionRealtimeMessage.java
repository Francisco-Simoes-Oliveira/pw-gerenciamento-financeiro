package com.financeiro.backend.features.realtime.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TransactionRealtimeMessage(
        String type,
        UUID transactionId,
        Set<UUID> walletIds,
        UUID changedByUserId,
        LocalDateTime occurredAt
) {
}
