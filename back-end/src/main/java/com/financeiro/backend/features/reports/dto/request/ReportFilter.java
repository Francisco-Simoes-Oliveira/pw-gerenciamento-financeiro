package com.financeiro.backend.features.reports.dto.request;

import java.time.LocalDate;
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
public class ReportFilter {
    private UUID walletId;
    private UUID categoryId;
    private UUID userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private TransactionType type;
    private TransactionStatus status;
}
