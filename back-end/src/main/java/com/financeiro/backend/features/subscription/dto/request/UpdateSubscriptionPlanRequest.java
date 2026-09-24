package com.financeiro.backend.features.subscription.dto.request;

import lombok.Data;

@Data
public class UpdateSubscriptionPlanRequest {
    private String name;
    private String description;
    private java.math.BigDecimal price;
    private Integer maxWallets;
    private Integer maxMembersPerWallet;
    private Integer maxCategories;
    private Boolean active;
    private Integer displayOrder;
}
