package com.financeiro.backend.features.wallet.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateWalletRequest {
    @NotBlank(message = "{name.obrigatorio}")
    private String name;
    private String description;
    private String currency;
    private String color;
    private String icon;
}
