package com.financeiro.backend.features.user.dto;

import java.util.Set;

public record UserResponse(Long id, String fullName, String email, Set<String> roles, boolean enabled) {
}
