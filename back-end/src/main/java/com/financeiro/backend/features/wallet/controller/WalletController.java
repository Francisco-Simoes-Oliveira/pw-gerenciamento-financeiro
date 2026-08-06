package com.financeiro.backend.features.wallet.controller;

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
import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;
import com.financeiro.backend.features.wallet.service.WalletService;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin
public class WalletController {

    @Autowired
    private WalletService service;

    @PostMapping
    public ResponseEntity<ApiResponse<WalletResponse>> insert(@RequestParam UUID ownerId, @Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.insert(ownerId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WalletResponse>>> listByOwner(@RequestParam UUID ownerId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByOwner(ownerId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> searchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.searchById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<WalletResponse>> alter(@PathVariable UUID id, @Valid @RequestBody UpdateWalletRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.alter(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id) {
        service.remove(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Carteira removida com sucesso."));
    }

    // Endpoints de Membros
    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> addMember(@PathVariable UUID id, @PathVariable UUID userId, @RequestParam String permission) {
        service.addMember(id, userId, permission);
        return ResponseEntity.ok(ApiResponse.success(null, "Membro adicionado com sucesso."));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeMember(@PathVariable UUID id, @PathVariable UUID userId) {
        service.removeMember(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Membro removido com sucesso."));
    }
}
