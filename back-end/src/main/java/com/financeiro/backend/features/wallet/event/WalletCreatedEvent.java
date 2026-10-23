package com.financeiro.backend.features.wallet.event;

import java.util.UUID;

public record WalletCreatedEvent(UUID userId, UUID walletId) {
}
