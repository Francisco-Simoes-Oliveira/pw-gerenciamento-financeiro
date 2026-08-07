package com.financeiro.backend.features.category.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.category.dto.request.CreateCategoryRequest;
import com.financeiro.backend.features.category.dto.request.UpdateCategoryRequest;
import com.financeiro.backend.features.category.dto.response.CategoryResponse;

public interface CategoryService {
    CategoryResponse insert(CreateCategoryRequest request);
    List<CategoryResponse> listByWallet(UUID walletId);
    CategoryResponse searchById(UUID id);
    CategoryResponse alter(UUID id, UpdateCategoryRequest request);
    void remove(UUID id);
}
