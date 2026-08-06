package com.financeiro.backend.features.subscription.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private Integer maxWallets;
    private Integer maxMembersPerWallet;
    private Integer maxCategories;
    private Boolean active;
    private Integer displayOrder;
}
