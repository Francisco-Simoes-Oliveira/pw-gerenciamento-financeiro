package com.financeiro.backend.features.reports.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CashFlowProjection {
    LocalDate getDate();
    BigDecimal getIncome();
    BigDecimal getExpense();
}
