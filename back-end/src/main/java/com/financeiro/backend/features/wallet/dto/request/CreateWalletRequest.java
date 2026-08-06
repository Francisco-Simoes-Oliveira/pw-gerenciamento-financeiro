package com.financeiro.backend.features.wallet.dto.request;

import lombok.Data;

@Data
public class CreateWalletRequest {
    private String name;
    private String description;
    private String currency;
    private String color;
    private String icon;
}
