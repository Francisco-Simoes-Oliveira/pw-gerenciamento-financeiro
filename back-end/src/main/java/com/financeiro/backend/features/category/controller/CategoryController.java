package com.financeiro.backend.features.category.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.category.dto.request.CreateCategoryRequest;
import com.financeiro.backend.features.category.dto.request.UpdateCategoryRequest;
import com.financeiro.backend.features.category.dto.response.CategoryResponse;
import com.financeiro.backend.features.category.service.CategoryService;
import com.financeiro.backend.security.services.UserDetailsImpl;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    @Autowired
    private CategoryService service;

    private UUID getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listByWallet(@RequestParam UUID walletId) {
        return ResponseEntity.ok(ApiResponse.success(service.listByWallet(walletId, getCurrentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> searchById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(service.searchById(id, getCurrentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> insert(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.insert(request, getCurrentUserId())));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> alter(@PathVariable UUID id, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.alter(id, request, getCurrentUserId())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id) {
        service.remove(id, getCurrentUserId());
        return ResponseEntity.ok(ApiResponse.success(null, "Categoria removida com sucesso."));
    }
}

