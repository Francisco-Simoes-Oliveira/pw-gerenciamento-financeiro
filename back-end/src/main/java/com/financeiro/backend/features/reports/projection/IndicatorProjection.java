package com.financeiro.backend.features.reports.projection;

import java.math.BigDecimal;

public interface IndicatorProjection {
    BigDecimal getMaxIncome();
    BigDecimal getMaxExpense();
    Long getTransactionCount();
    Long getCategoryCount();
}
