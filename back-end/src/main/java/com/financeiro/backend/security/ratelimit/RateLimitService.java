package com.financeiro.backend.security.ratelimit;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

/** Atomic fixed windows per route and identity. Suitable for one backend instance. */
@Service
public class RateLimitService {
    private final ConcurrentHashMap<Key, Window> windows = new ConcurrentHashMap<>();
    private final AtomicLong requests = new AtomicLong();
    private final Clock clock;

    public RateLimitService() {
        this(Clock.systemUTC());
    }

    // Clock injection keeps window-expiry tests deterministic.
    public RateLimitService(Clock clock) {
        this.clock = clock;
    }

    public Decision consume(RateLimitPolicy policy, String identity) {
        long now = clock.millis();
        long duration = policy.window().toMillis();
        var key = new Key(policy, identity);
        Window window = windows.compute(key, (ignored, current) -> {
            if (current == null || now >= current.expiresAtMillis()) {
                return new Window(now + duration, 1);
            }
            return new Window(current.expiresAtMillis(), current.count() + 1);
        });
        // Remove expired identities periodically so inactive clients do not remain forever.
        if (requests.incrementAndGet() % 256 == 0) {
            windows.entrySet().removeIf(entry -> entry.getValue().expiresAtMillis() <= now);
        }
        boolean allowed = window.count() <= policy.limit();
        long remainingMillis = Math.max(1, window.expiresAtMillis() - now);
        long retryAfterSeconds = Math.max(1, (remainingMillis + 999) / 1000);
        return new Decision(allowed, allowed ? 0 : retryAfterSeconds);
    }

    private record Key(RateLimitPolicy policy, String identity) {}
    private record Window(long expiresAtMillis, int count) {}
    public record Decision(boolean allowed, long retryAfterSeconds) {}
}
