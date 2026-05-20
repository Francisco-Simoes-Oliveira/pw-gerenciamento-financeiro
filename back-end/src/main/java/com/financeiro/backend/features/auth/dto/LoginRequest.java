package com.financeiro.backend.features.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
	@NotBlank(message = "E-mail é obrigatório.")
	@Email(message = "Informe um e-mail válido.")
	String email,

	@NotBlank(message = "Senha é obrigatória.")
	@Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres.")
	String password
) {
}
