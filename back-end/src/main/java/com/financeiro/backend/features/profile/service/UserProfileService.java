package com.financeiro.backend.features.profile.service;

import java.util.UUID;

import com.financeiro.backend.features.profile.dto.request.CreateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.request.UpdateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.response.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getByUserId(UUID userId);
    UserProfileResponse insert(CreateUserProfileRequest request);
    UserProfileResponse alter(UUID userId, UpdateUserProfileRequest request);
}
