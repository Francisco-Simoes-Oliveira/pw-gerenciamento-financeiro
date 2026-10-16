package com.financeiro.backend.features.intelligence.service;

import static org.junit.jupiter.api.Assertions.*;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.financeiro.backend.features.intelligence.exception.InvalidInsightTicketException;
import com.financeiro.backend.features.intelligence.model.InsightJob;

class InsightTicketServiceTest {
    private final MutableClock clock = new MutableClock();
    private final InsightTicketService service = new InsightTicketService(new SecureRandom(), clock);

    @Test void ticketIsBoundToAnalysisAndCanOnlyBeConsumedOnce() {
        var job = job();
        var ticket = service.issue(job);

        assertEquals(43, ticket.value().length());
        assertEquals(job, service.consume(job.analysisId(), ticket.value()));
        assertThrows(InvalidInsightTicketException.class,
                () -> service.consume(job.analysisId(), ticket.value()));
    }

    @Test void expiredOrMismatchedTicketIsRejected() {
        var job = job();
        var wrongAnalysisTicket = service.issue(job);
        assertThrows(InvalidInsightTicketException.class,
                () -> service.consume(UUID.randomUUID(), wrongAnalysisTicket.value()));

        var expiredTicket = service.issue(job);
        clock.advanceSeconds(60);
        assertThrows(InvalidInsightTicketException.class,
                () -> service.consume(job.analysisId(), expiredTicket.value()));
    }

    private InsightJob job() {
        return new InsightJob(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-10-15T12:00:00Z");
        void advanceSeconds(long seconds) { now = now.plusSeconds(seconds); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
