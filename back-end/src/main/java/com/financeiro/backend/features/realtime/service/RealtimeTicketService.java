package com.financeiro.backend.features.realtime.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.financeiro.backend.features.realtime.dto.RealtimeTicketResponse;

@Service
public class RealtimeTicketService {

    private static final Duration TICKET_TTL = Duration.ofSeconds(30);

    private final ConcurrentMap<String, TicketEntry> tickets = new ConcurrentHashMap<>();

    public RealtimeTicketResponse issue(UUID userId) {
        cleanupExpired();

        String ticket = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plus(TICKET_TTL);
        tickets.put(ticket, new TicketEntry(userId, expiresAt));

        return new RealtimeTicketResponse(ticket, expiresAt);
    }

    /**
     * Consome o ticket uma única vez. O JWT nunca precisa ser colocado na URL do WebSocket.
     */
    public Optional<UUID> consume(String ticket) {
        if (ticket == null || ticket.isBlank()) {
            return Optional.empty();
        }

        TicketEntry entry = tickets.remove(ticket);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            return Optional.empty();
        }

        return Optional.of(entry.userId());
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        tickets.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record TicketEntry(UUID userId, LocalDateTime expiresAt) {
    }
}
