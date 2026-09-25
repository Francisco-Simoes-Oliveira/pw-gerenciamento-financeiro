package com.financeiro.backend.features.realtime.dto;

import java.time.LocalDateTime;

public record RealtimeTicketResponse(String ticket, LocalDateTime expiresAt) {
}
