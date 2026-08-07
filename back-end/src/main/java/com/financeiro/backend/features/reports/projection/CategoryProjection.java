package com.financeiro.backend.features.reports.projection;

import java.math.BigDecimal;

public interface CategoryProjection {
    String getCategoryName();
    BigDecimal getTotal();
}
