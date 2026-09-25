package com.financeiro.backend.features.realtime.event;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record TransactionChangedEvent(
        ChangeType type,
        UUID transactionId,
        Set<UUID> walletIds,
        UUID changedByUserId,
        LocalDateTime occurredAt
) {
    public TransactionChangedEvent {
        walletIds = Set.copyOf(walletIds);
    }

    public enum ChangeType {
        CREATED,
        UPDATED,
        DELETED
    }
}
