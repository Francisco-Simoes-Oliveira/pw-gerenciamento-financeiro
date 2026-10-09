package com.financeiro.backend.security.ratelimit;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.security.services.UserDetailsImpl;

/** Runs after JWT authentication within the Spring Security filter chain. */
public class RateLimitFilter extends OncePerRequestFilter {
    private final RateLimitService service;
    private final ObjectMapper mapper = new ObjectMapper();

    public RateLimitFilter(RateLimitService service) {
        this.service = service;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return RateLimitPolicy.forRequest(request.getMethod(), request.getServletPath()) == null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var policy = RateLimitPolicy.forRequest(request.getMethod(), request.getServletPath());
        String identity;
        if (policy.perUser()) {
            var authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl user)) {
                // Leave unauthenticated ticket requests to the normal security chain (401).
                chain.doFilter(request, response);
                return;
            }
            identity = user.getId().toString();
        } else {
            // Never trust X-Forwarded-For supplied by a caller. Behind a proxy, configure
            // trusted forwarded-header handling at the deployment boundary.
            identity = request.getRemoteAddr();
            if (identity == null || identity.isBlank()) identity = "unknown";
        }

        var decision = service.consume(policy, identity);
        if (!decision.allowed()) {
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
            mapper.writeValue(response.getWriter(), ApiResponse.error("Muitas tentativas. Tente novamente mais tarde."));
            return;
        }
        chain.doFilter(request, response);
    }
}
