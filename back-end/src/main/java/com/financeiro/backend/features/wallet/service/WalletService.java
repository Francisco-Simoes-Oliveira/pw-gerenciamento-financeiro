package com.financeiro.backend.features.wallet.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;

public interface WalletService {
    WalletResponse insert(UUID ownerId, CreateWalletRequest request);
    List<WalletResponse> listByOwner(UUID ownerId);
    WalletResponse searchById(UUID id);
    WalletResponse alter(UUID id, UpdateWalletRequest request);
    void remove(UUID id);
    
    // Métodos para membros poderiam entrar aqui ou em um WalletMemberService separado
    void addMember(UUID walletId, UUID userId, String permission);
    void removeMember(UUID walletId, UUID userId);
}
