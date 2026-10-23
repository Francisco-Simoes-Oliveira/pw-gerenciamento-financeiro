package com.financeiro.backend.features.reports.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ReportViewedEvent(
        UUID userId,
        UUID walletId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal realizedBalance
) {
    public boolean isClosedPositiveMonth() {
        if (startDate == null || endDate == null || realizedBalance == null || realizedBalance.signum() <= 0) {
            return false;
        }
        boolean fullMonth = startDate.getDayOfMonth() == 1
                && endDate.equals(startDate.plusMonths(1).minusDays(1));
        boolean alreadyClosed = endDate.isBefore(LocalDate.now().withDayOfMonth(1));
        return fullMonth && alreadyClosed;
    }

    public String monthReference() {
        return walletId + ":" + startDate.getYear() + "-" + String.format("%02d", startDate.getMonthValue());
    }
}
