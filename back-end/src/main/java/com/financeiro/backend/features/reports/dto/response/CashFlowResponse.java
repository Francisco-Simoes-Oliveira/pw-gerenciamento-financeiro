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
public class CashFlowResponse {
    private LocalDate period; // Pode ser mês ou dia dependendo da agregação
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal periodBalance;
}
