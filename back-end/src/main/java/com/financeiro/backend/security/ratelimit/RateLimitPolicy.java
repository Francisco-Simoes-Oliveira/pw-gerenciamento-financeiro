package com.financeiro.backend.security.ratelimit;

import java.time.Duration;

/** Only these exact POST routes consume a rate limit. */
public enum RateLimitPolicy {
    LOGIN("/api/auth/login", 5, Duration.ofMinutes(1), false),
    PASSWORD_RESET_REQUEST("/api/auth/password-reset/request", 3, Duration.ofMinutes(10), false),
    PASSWORD_RESET_CONFIRM("/api/auth/password-reset/confirm", 5, Duration.ofMinutes(10), false),
    REGISTER("/api/users", 5, Duration.ofHours(1), false),
    REALTIME_TICKET("/api/realtime/ticket", 30, Duration.ofMinutes(1), true);

    private final String path;
    private final int limit;
    private final Duration window;
    private final boolean perUser;

    RateLimitPolicy(String path, int limit, Duration window, boolean perUser) {
        this.path = path;
        this.limit = limit;
        this.window = window;
        this.perUser = perUser;
    }

    public int limit() { return limit; }
    public Duration window() { return window; }
    public boolean perUser() { return perUser; }

    public static RateLimitPolicy forRequest(String method, String servletPath) {
        if (!"POST".equals(method)) return null;
        for (RateLimitPolicy policy : values()) {
            if (policy.path.equals(servletPath)) return policy;
        }
        return null;
    }
}
