package com.financeiro.backend.features.intelligence.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.financeiro.backend.features.intelligence.exception.InvalidInsightTicketException;
import com.financeiro.backend.features.intelligence.model.InsightJob;

@Service
public class InsightTicketService {
    static final Duration TICKET_TTL = Duration.ofSeconds(60);
    private final ConcurrentHashMap<String, StoredTicket> tickets = new ConcurrentHashMap<>();
    private final SecureRandom random;
    private final Clock clock;

    public InsightTicketService() {
        this(new SecureRandom(), Clock.systemUTC());
    }

    InsightTicketService(SecureRandom random, Clock clock) {
        this.random = random;
        this.clock = clock;
    }

    public IssuedTicket issue(InsightJob job) {
        cleanupExpired();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Instant expiresAt = clock.instant().plus(TICKET_TTL);
        tickets.put(hash(raw), new StoredTicket(job, expiresAt));
        return new IssuedTicket(raw, expiresAt);
    }

    public InsightJob consume(UUID analysisId, String rawTicket) {
        if (rawTicket == null || rawTicket.isBlank()) throw new InvalidInsightTicketException();
        StoredTicket stored = tickets.remove(hash(rawTicket));
        if (stored == null || !stored.job().analysisId().equals(analysisId)
                || !clock.instant().isBefore(stored.expiresAt())) {
            throw new InvalidInsightTicketException();
        }
        return stored.job();
    }

    private void cleanupExpired() {
        Instant now = clock.instant();
        tickets.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().expiresAt()));
    }

    private String hash(String value) {
        try {
            return Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponível.", ex);
        }
    }

    private record StoredTicket(InsightJob job, Instant expiresAt) {}
    public record IssuedTicket(String value, Instant expiresAt) {}
}
