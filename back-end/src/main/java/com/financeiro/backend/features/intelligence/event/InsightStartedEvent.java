package com.financeiro.backend.features.intelligence.event;

import java.util.UUID;

public record InsightStartedEvent(UUID userId, UUID analysisId) {
}
