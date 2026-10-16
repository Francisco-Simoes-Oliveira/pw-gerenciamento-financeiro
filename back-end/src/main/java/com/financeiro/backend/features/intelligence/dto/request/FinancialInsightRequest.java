package com.financeiro.backend.features.intelligence.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record FinancialInsightRequest(
        @NotNull UUID walletId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
