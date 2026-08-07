package com.financeiro.backend.features.reports.projection;

import java.math.BigDecimal;

public interface MonthlyProjection {
    Integer getYear();
    Integer getMonth();
    BigDecimal getIncome();
    BigDecimal getExpense();
}
