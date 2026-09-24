package com.financeiro.backend.features.subscription.service.impl;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.subscription.dto.request.CreateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.response.UserSubscriptionResponse;
import com.financeiro.backend.features.subscription.entity.SubscriptionPlan;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.enums.SubscriptionStatus;
import com.financeiro.backend.features.subscription.mapper.UserSubscriptionMapper;
import com.financeiro.backend.features.subscription.repository.SubscriptionPlanRepository;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;
import com.financeiro.backend.features.subscription.service.UserSubscriptionService;

@Service
public class UserSubscriptionServiceImpl implements UserSubscriptionService {

    @Autowired
    private UserSubscriptionRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private UserSubscriptionMapper mapper;

    @Override
    public UserSubscriptionResponse insert(CreateUserSubscriptionRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + request.getUserId()));

        SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado com ID: " + request.getPlanId()));

        UserSubscription subscription = mapper.toEntity(request);
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartDate(LocalDate.now());
        // Default plano de 30 dias para simplificar o exemplo
        subscription.setEndDate(LocalDate.now().plusDays(30));

        UserSubscription saved = repository.save(subscription);
        return mapper.toResponse(saved);
    }

    @Override
    public UserSubscriptionResponse searchById(UUID id) {
        UserSubscription subscription = findEntityById(id);
        return mapper.toResponse(subscription);
    }

    @Override
    public UserSubscriptionResponse searchByUserId(UUID userId) {
        UserSubscription subscription = repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura não encontrada para o usuário: " + userId));
        return mapper.toResponse(subscription);
    }

    @Override
    public UserSubscriptionResponse alter(UUID id, UpdateUserSubscriptionRequest request) {
        UserSubscription subscription = findEntityById(id);
        
        if (request.getPlanId() != null) {
            SubscriptionPlan plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado com ID: " + request.getPlanId()));
            subscription.setPlan(plan);
        }

        mapper.updateEntityFromDto(request, subscription);
        UserSubscription updated = repository.save(subscription);
        return mapper.toResponse(updated);
    }

    private UserSubscription findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assinatura não encontrada com o ID: " + id));
    }
}
