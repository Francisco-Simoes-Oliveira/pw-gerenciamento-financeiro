package com.financeiro.backend.features.wallet.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.financeiro.backend.features.wallet.enums.WalletPermission;

@Data
public class UpdateWalletMemberRequest {
    @NotNull
    private WalletPermission role;
}