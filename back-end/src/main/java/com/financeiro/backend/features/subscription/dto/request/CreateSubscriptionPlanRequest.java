package com.financeiro.backend.features.subscription.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSubscriptionPlanRequest {
    @NotBlank(message = "{name.obrigatorio}")
    private String name;
    
    private String description;
    
    @NotNull(message = "{price.obrigatorio}")
    private Double price;
    
    private Integer maxWallets;
    private Integer maxMembersPerWallet;
    private Integer maxCategories;
    
    private Boolean active;
    private Integer displayOrder;
}
