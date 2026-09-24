package com.financeiro.backend.features.reports.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyBalanceResponse {
    private String month; // e.g., "2026-01"
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal balance;
}
