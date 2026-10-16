package com.financeiro.backend.features.intelligence.model;

import java.time.LocalDate;
import java.util.UUID;

public record InsightJob(UUID analysisId, UUID userId, UUID walletId, LocalDate startDate, LocalDate endDate) {}
