package com.financeiro.backend.features.reports.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistoryResponse {
    private LocalDate date;
    private BigDecimal accumulatedBalance;
}
