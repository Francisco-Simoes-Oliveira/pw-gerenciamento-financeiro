package com.financeiro.backend.features.intelligence.controller;

import java.util.UUID;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.intelligence.dto.request.FinancialInsightRequest;
import com.financeiro.backend.features.intelligence.dto.response.InsightStartResponse;
import com.financeiro.backend.features.intelligence.service.FinancialInsightService;
import com.financeiro.backend.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/ai/insights")
@SecurityRequirement(name = "bearerAuth")
public class FinancialInsightController {
    private final FinancialInsightService service;

    public FinancialInsightController(FinancialInsightService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Autorizar e iniciar um Resumo Inteligente")
    public ResponseEntity<ApiResponse<InsightStartResponse>> start(
            @Valid @RequestBody FinancialInsightRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(ApiResponse.success(service.start(request, user.getId())));
    }

    @GetMapping(path = "/{analysisId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Acompanhar a análise via SSE com ticket efêmero")
    public SseEmitter stream(@PathVariable UUID analysisId, @RequestParam String ticket,
            HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Referrer-Policy", "no-referrer");
        return service.openStream(analysisId, ticket);
    }
}
