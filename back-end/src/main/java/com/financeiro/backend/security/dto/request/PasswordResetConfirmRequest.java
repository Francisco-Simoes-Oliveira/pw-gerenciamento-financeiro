package com.financeiro.backend.security.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetConfirmRequest {
    @NotBlank(message = "{token.obrigatorio}")
    private String token;

    @NotBlank(message = "{password.obrigatorio}")
    private String newPassword;
}
