package com.financeiro.backend.features.goals.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateGoalProgressRequest(
        @NotNull @DecimalMin("0.00") BigDecimal currentAmount
) {
}
