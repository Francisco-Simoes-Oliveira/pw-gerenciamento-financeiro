package com.financeiro.backend.features.wallet.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import com.financeiro.backend.features.wallet.enums.WalletPermission;

@Data
@Builder
public class WalletMemberResponse {
    private UUID userId;
    private String name;
    private String email;
    private WalletPermission role;
}