package com.financeiro.backend.features.goals.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.goals.dto.CreateGoalRequest;
import com.financeiro.backend.features.goals.dto.GoalResponse;
import com.financeiro.backend.features.goals.dto.UpdateGoalProgressRequest;
import com.financeiro.backend.features.goals.dto.UpdateGoalRequest;
import com.financeiro.backend.features.goals.service.FinancialGoalService;
import com.financeiro.backend.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/goals")
@SecurityRequirement(name = "bearerAuth")
public class FinancialGoalController {
    private final FinancialGoalService service;

    public FinancialGoalController(FinancialGoalService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> list(@AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponse.success(service.list(user.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> find(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponse.success(service.find(id, user.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> create(@Valid @RequestBody CreateGoalRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request, user.getId()), "Meta criada com sucesso."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GoalResponse>> update(@PathVariable UUID id,
            @Valid @RequestBody UpdateGoalRequest request, @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponse.success(service.update(id, request, user.getId())));
    }

    @PatchMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalResponse>> updateProgress(@PathVariable UUID id,
            @Valid @RequestBody UpdateGoalProgressRequest request, @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok(ApiResponse.success(service.updateProgress(id, request, user.getId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID id,
            @AuthenticationPrincipal UserDetailsImpl user) {
        service.cancel(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Meta cancelada com sucesso."));
    }
}
