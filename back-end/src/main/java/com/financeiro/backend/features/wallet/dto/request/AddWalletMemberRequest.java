package com.financeiro.backend.features.wallet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.financeiro.backend.features.wallet.enums.WalletPermission;

@Data
public class AddWalletMemberRequest {
    @NotBlank
    @Email
    private String email;

    @NotNull
    private WalletPermission role;
}