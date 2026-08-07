package com.financeiro.backend.features.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementResponse {
    private UUID transactionId;
    
    private String walletName;
    private String destinationWalletName;
    
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    
    private String createdByName;
    
    private TransactionType type;
    private TransactionStatus status;
    
    private String title;
    private String description;
    private String attachmentUrl;
    
    private BigDecimal amount;
    private BigDecimal balanceAfterOperation;
    
    private LocalDateTime transactionDate;
}
