package com.financeiro.backend.security.service;

import com.financeiro.backend.security.dto.request.PasswordResetConfirmRequest;
import com.financeiro.backend.security.dto.request.PasswordResetRequest;

public interface PasswordResetService {
    void requestPasswordReset(PasswordResetRequest request);
    void confirmPasswordReset(PasswordResetConfirmRequest request);
}
