package com.financeiro.backend.features.wallet.event;

import java.util.UUID;

public record WalletSharedEvent(UUID userId, UUID walletId) {
}
