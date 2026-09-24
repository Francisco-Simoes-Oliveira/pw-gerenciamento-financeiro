package com.financeiro.backend.features.profile.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserProfileRequest {
    @NotNull(message = "{user.obrigatorio}")
    private UUID userId;
    
    private String fullName;
    private LocalDate birthDate;
    private String phone;
    private String avatarUrl;
}
