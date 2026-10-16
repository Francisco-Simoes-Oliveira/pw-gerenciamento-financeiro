package com.financeiro.backend.features.intelligence.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.intelligence.dto.request.FinancialInsightRequest;
import com.financeiro.backend.features.intelligence.dto.response.InsightProgress;
import com.financeiro.backend.features.intelligence.dto.response.InsightResult;
import com.financeiro.backend.features.intelligence.dto.response.InsightStartResponse;
import com.financeiro.backend.features.intelligence.exception.InsightProviderException;
import com.financeiro.backend.features.intelligence.model.InsightJob;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@Service
public class FinancialInsightService {
    private static final long STREAM_TIMEOUT_MILLIS = 120_000;
    private final WalletRepository wallets;
    private final WalletAccessService access;
    private final InsightTicketService tickets;
    private final FinancialInsightAnalysisService analysis;
    private final ExecutorService executor;

    public FinancialInsightService(WalletRepository wallets, WalletAccessService access,
            InsightTicketService tickets, FinancialInsightAnalysisService analysis,
            @Qualifier("financialInsightExecutor") ExecutorService executor) {
        this.wallets = wallets;
        this.access = access;
        this.tickets = tickets;
        this.analysis = analysis;
        this.executor = executor;
    }

    public InsightStartResponse start(FinancialInsightRequest request, UUID userId) {
        validatePeriod(request);
        var wallet = wallets.findById(request.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        access.requireView(wallet, userId);
        UUID analysisId = UUID.randomUUID();
        var job = new InsightJob(analysisId, userId, request.walletId(), request.startDate(), request.endDate());
        var ticket = tickets.issue(job);
        return new InsightStartResponse(analysisId, ticket.value(), ticket.expiresAt(),
                "/api/ai/insights/" + analysisId + "/stream");
    }

    public SseEmitter openStream(UUID analysisId, String ticket) {
        InsightJob job = tickets.consume(analysisId, ticket);
        var emitter = new SseEmitter(STREAM_TIMEOUT_MILLIS);
        executor.execute(() -> runAnalysis(job, emitter));
        return emitter;
    }

    void runAnalysis(InsightJob job, SseEmitter emitter) {
        var eventId = new AtomicLong();
        try {
            InsightResult result = analysis.analyze(job,
                    progress -> send(emitter, eventId, "progress", progress));
            send(emitter, eventId, "complete", result);
            emitter.complete();
        } catch (UncheckedIOException ex) {
            emitter.complete();
        } catch (Exception ex) {
            try {
                String message = ex instanceof InsightProviderException
                        && "Serviço de IA não configurado.".equals(ex.getMessage())
                        ? ex.getMessage()
                        : "Não foi possível concluir a análise agora. Tente novamente mais tarde.";
                send(emitter, eventId, "analysis-error", Map.of("message", message));
                emitter.complete();
            } catch (UncheckedIOException ignored) {
                emitter.complete();
            }
        }
    }

    private void send(SseEmitter emitter, AtomicLong id, String name, Object data) {
        try {
            emitter.send(SseEmitter.event().id(Long.toString(id.incrementAndGet())).name(name).data(data));
        } catch (IOException | IllegalStateException ex) {
            throw new UncheckedIOException(new IOException("Conexão SSE encerrada.", ex));
        }
    }

    private void validatePeriod(FinancialInsightRequest request) {
        if (request == null || request.walletId() == null || request.startDate() == null || request.endDate() == null
                || request.endDate().isBefore(request.startDate())
                || request.startDate().getYear() < 1900 || request.endDate().getYear() > 9998
                || ChronoUnit.DAYS.between(request.startDate(), request.endDate()) >= 366) {
            throw new IllegalArgumentException("Informe uma carteira e um período válido de até 366 dias.");
        }
    }
}
