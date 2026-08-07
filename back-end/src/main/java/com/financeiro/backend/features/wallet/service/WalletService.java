package com.financeiro.backend.features.wallet.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;

public interface WalletService {
    WalletResponse insert(UUID ownerId, CreateWalletRequest request);
    List<WalletResponse> listByOwner(UUID ownerId);
    WalletResponse searchById(UUID id, UUID currentUserId);
    WalletResponse alter(UUID id, UUID currentUserId, UpdateWalletRequest request);
    void remove(UUID id, UUID currentUserId);
    
    void addMember(UUID walletId, UUID currentUserId, UUID targetUserId, String permission);
    void removeMember(UUID walletId, UUID currentUserId, UUID targetUserId);
}
