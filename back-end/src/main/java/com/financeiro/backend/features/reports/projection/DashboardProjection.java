package com.financeiro.backend.features.reports.projection;

import java.math.BigDecimal;

public interface DashboardProjection {
    BigDecimal getIncome();
    BigDecimal getExpense();
    BigDecimal getTransfer();
    Long getTransactionsCount();
}
