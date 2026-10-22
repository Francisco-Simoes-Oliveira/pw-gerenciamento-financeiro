package com.financeiro.backend.features.gamification.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.gamification.dto.GamificationProfileResponse;
import com.financeiro.backend.features.gamification.service.GamificationService;
import com.financeiro.backend.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/gamification")
@SecurityRequirement(name = "bearerAuth")
public class GamificationController {
    private final GamificationService service;

    public GamificationController(GamificationService service) {
        this.service = service;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<GamificationProfileResponse>> profile(
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.success(service.getProfile(user.getId())));
    }
}
