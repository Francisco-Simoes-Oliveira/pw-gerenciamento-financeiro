package com.financeiro.backend.features.subscription.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.financeiro.backend.features.subscription.enums.SubscriptionStatus;

import lombok.Data;

@Data
public class UserSubscriptionResponse {
    private UUID id;
    private UUID userId;
    private UUID planId;
    private SubscriptionStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean autoRenew;
}
