package com.financeiro.backend.features.subscription.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserSubscriptionRequest {
    @NotNull(message = "{user.obrigatorio}")
    private UUID userId;

    @NotNull(message = "{plan.obrigatorio}")
    private UUID planId;

    private Boolean autoRenew;
}
