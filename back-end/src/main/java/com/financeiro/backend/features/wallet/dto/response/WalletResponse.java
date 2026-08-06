package com.financeiro.backend.features.wallet.dto.response;

import java.util.UUID;
import lombok.Data;

@Data
public class WalletResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String description;
    private String currency;
    private Boolean active;
    private String color;
    private String icon;
}
