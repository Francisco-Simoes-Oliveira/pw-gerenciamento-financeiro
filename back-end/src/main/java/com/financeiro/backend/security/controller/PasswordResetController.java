package com.financeiro.backend.security.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.security.dto.request.PasswordResetConfirmRequest;
import com.financeiro.backend.security.dto.request.PasswordResetRequest;
import com.financeiro.backend.security.service.PasswordResetService;

@RestController
@RequestMapping("/api/auth/password-reset")
@CrossOrigin
public class PasswordResetController {

    @Autowired
    private PasswordResetService service;

    @PostMapping("/request")
    public ResponseEntity<ApiResponse<Void>> requestReset(@Valid @RequestBody PasswordResetRequest request) {
        service.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Instruções de redefinição de senha enviadas por email."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        service.confirmPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Senha alterada com sucesso."));
    }
}
