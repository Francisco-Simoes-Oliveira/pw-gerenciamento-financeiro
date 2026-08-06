package com.financeiro.backend.features.subscription.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.response.SubscriptionPlanResponse;

public interface SubscriptionPlanService {
    SubscriptionPlanResponse insert(CreateSubscriptionPlanRequest request);
    List<SubscriptionPlanResponse> listAll();
    SubscriptionPlanResponse searchById(UUID id);
    SubscriptionPlanResponse alter(UUID id, UpdateSubscriptionPlanRequest request);
    void remove(UUID id);
}
