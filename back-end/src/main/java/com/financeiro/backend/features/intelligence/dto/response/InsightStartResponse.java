package com.financeiro.backend.features.intelligence.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InsightStartResponse(UUID analysisId, String ticket, Instant expiresAt, String streamPath) {}
