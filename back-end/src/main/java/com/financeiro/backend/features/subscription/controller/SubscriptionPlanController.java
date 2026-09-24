package com.financeiro.backend.features.subscription.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.response.SubscriptionPlanResponse;
import com.financeiro.backend.features.subscription.service.SubscriptionPlanService;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(service.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> searchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.searchById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> insert(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.insert(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> alter(@PathVariable UUID id, @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.alter(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id) {
        service.remove(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Plano excluído com sucesso."));
    }
}
