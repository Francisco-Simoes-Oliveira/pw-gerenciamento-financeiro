package com.financeiro.backend.features.profile.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private String fullName;
    private LocalDate birthDate;
    private String phone;
    private String avatarUrl;
}
