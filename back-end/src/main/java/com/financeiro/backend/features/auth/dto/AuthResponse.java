package com.financeiro.backend.features.auth.dto;

import com.financeiro.backend.features.user.dto.UserResponse;

public record AuthResponse(String accessToken, String tokenType, long expiresInMinutes, UserResponse user) {
}
