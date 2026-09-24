package com.financeiro.backend.features.profile.controller;

import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.profile.dto.request.CreateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.request.UpdateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.response.UserProfileResponse;
import com.financeiro.backend.features.profile.service.UserProfileService;

@RestController
@RequestMapping("/api/users/{userId}/profile")
@CrossOrigin
public class UserProfileController {

    @Autowired
    private UserProfileService service;

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(service.getByUserId(userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> createProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateUserProfileRequest request) {
        
        request.setUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(service.insert(request)));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        
        return ResponseEntity.ok(ApiResponse.success(service.alter(userId, request)));
    }
}
