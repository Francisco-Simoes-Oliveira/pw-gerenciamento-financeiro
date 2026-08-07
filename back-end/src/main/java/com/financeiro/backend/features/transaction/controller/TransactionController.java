package com.financeiro.backend.features.transaction.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.request.UpdateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;
import com.financeiro.backend.features.transaction.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin
public class TransactionController {

    @Autowired
    private TransactionService service;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> insert(@RequestParam UUID currentUserId, @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.insert(currentUserId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> listByWallet(@RequestParam UUID walletId, @RequestParam UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByWallet(walletId, currentUserId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> searchById(@PathVariable UUID id, @RequestParam UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.searchById(id, currentUserId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> alter(@PathVariable UUID id, @RequestParam UUID currentUserId, @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.alter(id, currentUserId, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id, @RequestParam UUID currentUserId) {
        service.remove(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "Transação removida com sucesso."));
    }
}
