package com.financeiro.backend.features.transaction.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;

@Data
public class TransactionResponse {
    private UUID id;
    private UUID walletId;
    private UUID destinationWalletId;
    private UUID categoryId;
    private UUID createdById;
    private String title;
    private String description;
    private java.math.BigDecimal amount;
    private LocalDateTime transactionDate;
    private TransactionType type;
    private TransactionStatus status;
    private String attachmentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
