package com.financeiro.backend.features.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(
	@NotBlank(message = "Nome completo é obrigatório.")
	@Size(max = 120, message = "Nome completo deve ter no máximo 120 caracteres.")
	String fullName,

	@NotBlank(message = "E-mail é obrigatório.")
	@Email(message = "Informe um e-mail válido.")
	@Size(max = 180, message = "E-mail deve ter no máximo 180 caracteres.")
	String email,

	@NotBlank(message = "Senha é obrigatória.")
	@Size(min = 8, max = 72, message = "Senha deve ter entre 8 e 72 caracteres.")
	String password
) {
}
