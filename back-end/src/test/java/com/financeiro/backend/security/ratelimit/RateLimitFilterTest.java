package com.financeiro.backend.security.ratelimit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.servlet.ServletException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeiro.backend.security.services.UserDetailsImpl;

class RateLimitFilterTest {
    private final MutableClock clock = new MutableClock();
    private final RateLimitFilter filter = new RateLimitFilter(new RateLimitService(clock));
    private final ObjectMapper mapper = new ObjectMapper();

    @AfterEach void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    private MockHttpServletResponse request(String method, String path, String ip, AtomicInteger forwarded)
            throws IOException, ServletException {
        return request(method, path, ip, forwarded, null);
    }

    private MockHttpServletResponse request(String method, String path, String ip, AtomicInteger forwarded, String spoofedIp)
            throws IOException, ServletException {
        var req = new MockHttpServletRequest(method, path);
        req.setServletPath(path);
        req.setRemoteAddr(ip);
        if (spoofedIp != null) req.addHeader("X-Forwarded-For", spoofedIp);
        var response = new MockHttpServletResponse();
        filter.doFilter(req, response, (ignoredRequest, ignoredResponse) -> forwarded.incrementAndGet());
        return response;
    }

    @Test void fiveLoginsPassAndSixthIsStandardJson429WithRetryAfter() throws Exception {
        var forwarded = new AtomicInteger();
        for (int i = 0; i < 5; i++) {
            assertEquals(200, request("POST", "/api/auth/login", "192.0.2.1", forwarded).getStatus());
        }
        var blocked = request("POST", "/api/auth/login", "192.0.2.1", forwarded);
        assertEquals(5, forwarded.get());
        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));
        assertEquals("no-store", blocked.getHeader("Cache-Control"));
        assertTrue(blocked.getContentType().startsWith("application/json"));
        var json = mapper.readTree(blocked.getContentAsString());
        assertFalse(json.get("success").asBoolean());
        assertTrue(json.get("message").asText().contains("Muitas tentativas"));
        assertTrue(json.get("data").isNull());
    }

    @Test void ipsAreIndependentAndCallerCannotRotateCounterWithForwardedHeader() throws Exception {
        var forwarded = new AtomicInteger();
        for (int i = 0; i < 5; i++) request("POST", "/api/auth/login", "192.0.2.1", forwarded, "203.0.113." + i);
        assertEquals(429, request("POST", "/api/auth/login", "192.0.2.1", forwarded, "203.0.113.250").getStatus());
        assertEquals(200, request("POST", "/api/auth/login", "192.0.2.2", forwarded).getStatus());
        assertEquals(6, forwarded.get());
    }

    @Test void windowExpiresAndRetryAfterRoundsUp() throws Exception {
        var forwarded = new AtomicInteger();
        for (int i = 0; i < 5; i++) request("POST", "/api/auth/login", "192.0.2.3", forwarded);
        clock.advanceMillis(1_001);
        assertEquals("59", request("POST", "/api/auth/login", "192.0.2.3", forwarded).getHeader("Retry-After"));
        clock.advanceMillis(58_999);
        assertEquals(200, request("POST", "/api/auth/login", "192.0.2.3", forwarded).getStatus());
        assertEquals(6, forwarded.get());
    }

    @Test void actualResetRegisterRoutesHaveIndependentLimits() throws Exception {
        var forwarded = new AtomicInteger();
        for (int i = 0; i < 3; i++) request("POST", "/api/auth/password-reset/request", "192.0.2.4", forwarded);
        assertEquals(429, request("POST", "/api/auth/password-reset/request", "192.0.2.4", forwarded).getStatus());
        for (int i = 0; i < 5; i++) request("POST", "/api/auth/password-reset/confirm", "192.0.2.4", forwarded);
        assertEquals(429, request("POST", "/api/auth/password-reset/confirm", "192.0.2.4", forwarded).getStatus());
        for (int i = 0; i < 5; i++) request("POST", "/api/users", "192.0.2.4", forwarded);
        var blocked = request("POST", "/api/users", "192.0.2.4", forwarded);
        assertEquals(429, blocked.getStatus());
        assertEquals("3600", blocked.getHeader("Retry-After"));
    }

    @Test void ticketIsLimitedPerAuthenticatedUserAndAnonymousRequestKeepsNormalSecurityPath() throws Exception {
        var forwarded = new AtomicInteger();
        UUID userA = UUID.randomUUID(), userB = UUID.randomUUID();
        authenticate(userA);
        for (int i = 0; i < 30; i++) request("POST", "/api/realtime/ticket", "192.0.2.5", forwarded);
        assertEquals(429, request("POST", "/api/realtime/ticket", "192.0.2.6", forwarded).getStatus());
        authenticate(userB);
        assertEquals(200, request("POST", "/api/realtime/ticket", "192.0.2.5", forwarded).getStatus());
        SecurityContextHolder.clearContext();
        assertEquals(200, request("POST", "/api/realtime/ticket", "192.0.2.5", forwarded).getStatus());
        assertEquals(32, forwarded.get());
    }

    private void authenticate(UUID id) {
        var user = new UserDetailsImpl(id, "User", "test@example.test", "", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test void unrelatedPathsAndMethodsRemainUnlimited() throws Exception {
        var forwarded = new AtomicInteger();
        for (int i = 0; i < 50; i++) {
            request("POST", "/api/reports/statement/report", "192.0.2.7", forwarded);
            request("GET", "/api/auth/login", "192.0.2.7", forwarded);
            request("POST", "/api/auth/login/extra", "192.0.2.7", forwarded);
        }
        assertEquals(150, forwarded.get());
    }

    @Test void concurrentRequestsCannotExceedQuota() {
        var service = new RateLimitService(clock);
        var tasks = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> service.consume(RateLimitPolicy.LOGIN, "192.0.2.8").allowed()))
                .toList();
        assertEquals(5, tasks.stream().filter(CompletableFuture::join).count());
    }

    private static class MutableClock extends Clock {
        private final AtomicLong millis = new AtomicLong(Instant.parse("2026-10-08T12:00:00Z").toEpochMilli());
        void advanceMillis(long amount) { millis.addAndGet(amount); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public long millis() { return millis.get(); }
        @Override public Instant instant() { return Instant.ofEpochMilli(millis()); }
    }
}
