package com.financeiro.backend.features.subscription.service.impl;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.response.SubscriptionPlanResponse;
import com.financeiro.backend.features.subscription.entity.SubscriptionPlan;
import com.financeiro.backend.features.subscription.mapper.SubscriptionPlanMapper;
import com.financeiro.backend.features.subscription.repository.SubscriptionPlanRepository;
import com.financeiro.backend.features.subscription.service.SubscriptionPlanService;

@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    @Autowired
    private SubscriptionPlanRepository repository;

    @Autowired
    private SubscriptionPlanMapper mapper;

    @Override
    public SubscriptionPlanResponse insert(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = mapper.toEntity(request);
        if (plan.getActive() == null) {
            plan.setActive(true);
        }
        SubscriptionPlan saved = repository.save(plan);
        return mapper.toResponse(saved);
    }

    @Override
    public List<SubscriptionPlanResponse> listAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanResponse searchById(UUID id) {
        SubscriptionPlan plan = findEntityById(id);
        return mapper.toResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse alter(UUID id, UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = findEntityById(id);
        mapper.updateEntityFromDto(request, plan);
        SubscriptionPlan updated = repository.save(plan);
        return mapper.toResponse(updated);
    }

    @Override
    public void remove(UUID id) {
        SubscriptionPlan plan = findEntityById(id);
        repository.delete(plan);
    }

    private SubscriptionPlan findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado com o ID: " + id));
    }
}
