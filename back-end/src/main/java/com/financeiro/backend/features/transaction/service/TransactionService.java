package com.financeiro.backend.features.transaction.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.request.UpdateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;

public interface TransactionService {
    TransactionResponse insert(UUID currentUserId, CreateTransactionRequest request);
    List<TransactionResponse> listByWallet(UUID walletId, UUID currentUserId);
    TransactionResponse searchById(UUID id, UUID currentUserId);
    TransactionResponse alter(UUID id, UUID currentUserId, UpdateTransactionRequest request);
    void remove(UUID id, UUID currentUserId);
}
