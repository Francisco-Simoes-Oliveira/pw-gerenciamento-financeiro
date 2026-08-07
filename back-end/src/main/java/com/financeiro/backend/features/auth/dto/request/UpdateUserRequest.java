package com.financeiro.backend.features.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "{name.obrigatorio}")
    private String name;

    @Email(message = "{email.invalido}")
    @NotBlank(message = "{email.obrigatorio}")
    private String email;

    private String password;
}
