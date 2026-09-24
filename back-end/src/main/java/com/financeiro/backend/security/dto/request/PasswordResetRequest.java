package com.financeiro.backend.security.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetRequest {
    @Email(message = "{email.invalido}")
    @NotBlank(message = "{email.obrigatorio}")
    private String email;
}
