package com.financeiro.backend.features.wallet.dto.request;

import lombok.Data;

@Data
public class UpdateWalletRequest {
    private String name;
    private String description;
    private String currency;
    private String color;
    private String icon;
    private Boolean active;
}
