package com.financeiro.backend.features.intelligence.model;

import java.math.BigDecimal;
import java.util.List;

/** Deliberately contains no user, wallet, transaction IDs, titles, descriptions or email. */
public record FinancialInsightData(
        String periodStart,
        String periodEnd,
        String currency,
        PeriodTotals current,
        PeriodTotals previous,
        BigDecimal expenseChangePercent,
        long paidTransactionCount,
        List<CategoryComparison> categories
) {
    public record PeriodTotals(BigDecimal income, BigDecimal expense, BigDecimal transferIn,
            BigDecimal transferOut, BigDecimal netChange) {}
    public record CategoryComparison(String name, BigDecimal currentTotal, BigDecimal previousTotal) {}
}
