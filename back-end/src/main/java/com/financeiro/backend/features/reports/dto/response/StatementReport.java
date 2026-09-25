package com.financeiro.backend.features.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.transaction.enums.TransactionStatus;

/** A single authorized snapshot, shared by the screen and both exports. */
public record StatementReport(UUID walletId, String walletName, String currency,
        LocalDate startDate, LocalDate endDate, LocalDateTime generatedAt,
        BigDecimal income, BigDecimal expense, BigDecimal transferIn, BigDecimal transferOut,
        BigDecimal netChange, long pendingCount, long canceledCount, List<Entry> entries) {

    public record Entry(UUID id, LocalDateTime date, String title, String category,
            String movement, TransactionStatus status, BigDecimal amount, BigDecimal impact) {}
}
