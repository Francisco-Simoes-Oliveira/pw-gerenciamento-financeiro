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
public class IndicatorsResponse {
    private BigDecimal maxIncome;
    private BigDecimal maxExpense;
    private BigDecimal dailyAverage;
    private BigDecimal monthlyAverage;
    private Long transactionCount;
    private BigDecimal averageTicket; // Ticket Médio
    private String topCategory;
    private String topWallet;
}
