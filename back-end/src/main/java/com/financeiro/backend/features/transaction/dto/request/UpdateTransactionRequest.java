package com.financeiro.backend.features.transaction.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;

@Data
public class UpdateTransactionRequest {
    private UUID categoryId;
    private UUID destinationWalletId;
    private String title;
    private String description;
    @jakarta.validation.constraints.Positive(message = "{amount.positivo}")
    private java.math.BigDecimal amount;
    private LocalDateTime transactionDate;
    private TransactionType type;
    private TransactionStatus status;
    private String attachmentUrl;
}
