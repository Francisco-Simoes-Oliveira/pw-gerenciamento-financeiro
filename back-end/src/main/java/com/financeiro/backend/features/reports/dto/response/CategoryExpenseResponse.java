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
public class CategoryExpenseResponse {
    private String category;
    private BigDecimal total;
    private Double percentage;
}
