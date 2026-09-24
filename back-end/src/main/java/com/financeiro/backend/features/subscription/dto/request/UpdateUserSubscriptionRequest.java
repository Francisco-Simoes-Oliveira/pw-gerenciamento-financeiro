package com.financeiro.backend.features.subscription.dto.request;

import java.util.UUID;

import com.financeiro.backend.features.subscription.enums.SubscriptionStatus;

import lombok.Data;

@Data
public class UpdateUserSubscriptionRequest {
    private UUID planId;
    private SubscriptionStatus status;
    private Boolean autoRenew;
}
