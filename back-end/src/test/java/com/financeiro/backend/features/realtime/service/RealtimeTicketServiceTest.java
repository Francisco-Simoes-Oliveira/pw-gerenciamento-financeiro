package com.financeiro.backend.features.realtime.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class RealtimeTicketServiceTest {

    private final RealtimeTicketService service = new RealtimeTicketService();

    @Test
    void ticketShouldBeSingleUse() {
        UUID userId = UUID.randomUUID();
        var issued = service.issue(userId);

        assertEquals(userId, service.consume(issued.ticket()).orElseThrow());
        assertTrue(service.consume(issued.ticket()).isEmpty());
    }

    @Test
    void blankTicketShouldBeRejected() {
        assertTrue(service.consume(null).isEmpty());
        assertTrue(service.consume(" ").isEmpty());
    }
}
