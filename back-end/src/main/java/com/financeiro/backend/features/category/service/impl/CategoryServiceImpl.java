package com.financeiro.backend.features.category.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.category.dto.request.CreateCategoryRequest;
import com.financeiro.backend.features.category.dto.request.UpdateCategoryRequest;
import com.financeiro.backend.features.category.dto.response.CategoryResponse;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.category.mapper.CategoryMapper;
import com.financeiro.backend.features.category.repository.CategoryRepository;
import com.financeiro.backend.features.category.service.CategoryService;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository repository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserSubscriptionRepository subscriptionRepository;

    @Autowired
    private CategoryMapper mapper;

    @Override
    public CategoryResponse insert(CreateCategoryRequest request, UUID currentUserId) {
        if (request.getWalletId() != null && repository.existsByWalletIdAndName(request.getWalletId(), request.getName())) {
            throw new IllegalArgumentException("Já existe uma categoria com este nome na carteira.");
        }

        Category category = mapper.toEntity(request);
        
        if (request.getWalletId() != null) {
            Wallet wallet = walletRepository.findById(request.getWalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
            
            if (!wallet.getOwner().getId().equals(currentUserId)) {
                throw new SecurityException("Acesso negado à carteira.");
            }
                    
            UserSubscription subscription = subscriptionRepository.findByUserId(wallet.getOwner().getId())
                    .orElseThrow(() -> new IllegalStateException("O proprietário precisa de uma assinatura ativa."));
                    
            Integer maxCategories = subscription.getPlan().getMaxCategories();
            if (maxCategories != null) {
                long currentCategories = repository.countByWalletId(wallet.getId());
                if (currentCategories >= maxCategories) {
                    throw new IllegalArgumentException("Limite de categorias do plano atingido.");
                }
            }
            
            category.setWallet(wallet);
        }
        
        if (category.getActive() == null) {
            category.setActive(true);
        }
        if (category.getSystemCategory() == null) {
            category.setSystemCategory(false);
        }

        Category saved = repository.save(category);
        return mapper.toResponse(saved);
    }

    @Override
    public List<CategoryResponse> listByWallet(UUID walletId, UUID currentUserId) {
        Wallet wallet = walletRepository.findById(walletId)
            .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        if (!wallet.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Acesso negado à carteira.");
        }
        return repository.findAll().stream()
                .filter(c -> c.getWallet() != null && c.getWallet().getId().equals(walletId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse searchById(UUID id, UUID currentUserId) {
        Category category = findEntityById(id);
        if (category.getWallet() != null && !category.getWallet().getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Acesso negado à categoria.");
        }
        return mapper.toResponse(category);
    }

    @Override
    public CategoryResponse alter(UUID id, UpdateCategoryRequest request, UUID currentUserId) {
        Category category = findEntityById(id);
        
        if (category.getWallet() != null && !category.getWallet().getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Acesso negado à categoria.");
        }
        
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (category.getWallet() != null && repository.existsByWalletIdAndName(category.getWallet().getId(), request.getName())) {
                throw new IllegalArgumentException("Já existe uma categoria com este nome na carteira.");
            }
        }
        
        mapper.updateEntityFromDto(request, category);
        Category updated = repository.save(category);
        return mapper.toResponse(updated);
    }

    @Override
    public void remove(UUID id, UUID currentUserId) {
        Category category = findEntityById(id);
        if (category.getWallet() != null && !category.getWallet().getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Acesso negado à categoria.");
        }
        if (Boolean.TRUE.equals(category.getSystemCategory())) {
            throw new IllegalArgumentException("Categorias de sistema não podem ser excluídas.");
        }
        repository.delete(category);
    }

    private Category findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + id));
    }
}

