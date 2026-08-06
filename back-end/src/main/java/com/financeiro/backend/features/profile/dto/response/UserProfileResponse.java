package com.financeiro.backend.features.profile.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class UserProfileResponse {
    private UUID id;
    private UUID userId;
    private String fullName;
    private LocalDate birthDate;
    private String phone;
    private String avatarUrl;
}
