package com.financeiro.backend.features.goals.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGoalRequest(
        @NotNull UUID walletId,
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin("0.01") BigDecimal targetAmount,
        @DecimalMin("0.00") BigDecimal currentAmount,
        LocalDate deadline
) {
}
