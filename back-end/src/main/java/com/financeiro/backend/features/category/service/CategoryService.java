package com.financeiro.backend.features.category.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.category.dto.request.CreateCategoryRequest;
import com.financeiro.backend.features.category.dto.request.UpdateCategoryRequest;
import com.financeiro.backend.features.category.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse insert(CreateCategoryRequest request, UUID currentUserId);
    List<CategoryResponse> listByWallet(UUID walletId, UUID currentUserId);
    CategoryResponse searchById(UUID id, UUID currentUserId);
    CategoryResponse alter(UUID id, UpdateCategoryRequest request, UUID currentUserId);
    void remove(UUID id, UUID currentUserId);
}

