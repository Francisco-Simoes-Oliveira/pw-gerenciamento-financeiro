package com.financeiro.backend.features.subscription.controller;

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
import com.financeiro.backend.features.subscription.dto.request.CreateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.response.UserSubscriptionResponse;
import com.financeiro.backend.features.subscription.service.UserSubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin
public class UserSubscriptionController {

    @Autowired
    private UserSubscriptionService service;

    @PostMapping
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> create(@Valid @RequestBody CreateUserSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.insert(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> searchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.searchById(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> searchByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(service.searchByUserId(userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserSubscriptionResponse>> alter(@PathVariable UUID id, @Valid @RequestBody UpdateUserSubscriptionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.alter(id, request)));
    }
}
