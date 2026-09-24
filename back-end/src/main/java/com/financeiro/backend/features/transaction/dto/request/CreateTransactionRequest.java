package com.financeiro.backend.features.transaction.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;

@Data
public class CreateTransactionRequest {
    @NotNull(message = "{wallet.obrigatorio}")
    private UUID walletId;
    
    private UUID destinationWalletId;

    @NotNull(message = "{category.obrigatorio}")
    private UUID categoryId;

    @NotBlank(message = "{title.obrigatorio}")
    private String title;

    private String description;

    @NotNull(message = "{amount.obrigatorio}")
    @jakarta.validation.constraints.Positive(message = "{amount.positivo}")
    private java.math.BigDecimal amount;
    
    @NotNull(message = "{transactionDate.obrigatorio}")
    private LocalDateTime transactionDate;

    @NotNull(message = "{type.obrigatorio}")
    private TransactionType type;

    @NotNull(message = "{status.obrigatorio}")
    private TransactionStatus status;
    
    private String attachmentUrl;
}
