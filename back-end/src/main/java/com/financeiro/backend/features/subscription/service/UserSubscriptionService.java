package com.financeiro.backend.features.subscription.service;

import java.util.UUID;

import com.financeiro.backend.features.subscription.dto.request.CreateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.response.UserSubscriptionResponse;

public interface UserSubscriptionService {
    UserSubscriptionResponse insert(CreateUserSubscriptionRequest request);
    UserSubscriptionResponse searchById(UUID id);
    UserSubscriptionResponse searchByUserId(UUID userId);
    UserSubscriptionResponse alter(UUID id, UpdateUserSubscriptionRequest request);
}
