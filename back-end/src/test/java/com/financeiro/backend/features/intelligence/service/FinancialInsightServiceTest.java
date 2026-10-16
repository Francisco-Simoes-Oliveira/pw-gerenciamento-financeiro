package com.financeiro.backend.features.intelligence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.financeiro.backend.features.intelligence.dto.request.FinancialInsightRequest;
import com.financeiro.backend.features.intelligence.dto.response.InsightProgress;
import com.financeiro.backend.features.intelligence.dto.response.InsightResult;
import com.financeiro.backend.features.intelligence.exception.InsightProviderException;
import com.financeiro.backend.features.intelligence.model.InsightJob;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

class FinancialInsightServiceTest {
    private final WalletRepository wallets = mock(WalletRepository.class);
    private final WalletAccessService access = mock(WalletAccessService.class);
    private final InsightTicketService tickets = mock(InsightTicketService.class);
    private final FinancialInsightAnalysisService analysis = mock(FinancialInsightAnalysisService.class);
    private final FinancialInsightService service = new FinancialInsightService(wallets, access, tickets, analysis,
            mock(ExecutorService.class));
    private final UUID walletId = UUID.randomUUID(), userId = UUID.randomUUID();

    @Test void startRequiresViewPermissionAndIssuesTicketOnlyAfterAuthorization() {
        var wallet = Wallet.builder().id(walletId).name("Compartilhada").build();
        var request = new FinancialInsightRequest(walletId, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
        var jobTicket = new InsightTicketService.IssuedTicket("ticket", java.time.Instant.parse("2026-10-15T12:01:00Z"));
        when(wallets.findById(walletId)).thenReturn(java.util.Optional.of(wallet));
        when(tickets.issue(any())).thenReturn(jobTicket);

        var response = service.start(request, userId);

        assertEquals("ticket", response.ticket());
        verify(access).requireView(wallet, userId);
        verify(tickets).issue(argThat(job -> job.userId().equals(userId) && job.walletId().equals(walletId)));

        doThrow(new AccessDeniedException("Sem acesso")).when(access).requireView(wallet, userId);
        assertThrows(AccessDeniedException.class, () -> service.start(request, userId));
        verify(tickets, times(1)).issue(any());
    }

    @Test void runAnalysisEmitsProgressThenCompletion() {
        var result = new InsightResult("Resumo", List.of("Destaque"), List.of("Ação"), "Aviso");
        when(analysis.analyze(any(), any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            var progress = (java.util.function.Consumer<InsightProgress>) invocation.getArgument(1);
            progress.accept(new InsightProgress("PREPARING_DATA", "Preparando dados..."));
            return result;
        });
        var emitter = new CapturingEmitter();

        service.runAnalysis(job(), emitter);

        assertEquals(2, emitter.events.size());
        assertTrue(emitter.events.get(0).stream().anyMatch(value -> value.toString().contains("event:progress")), emitter.events.toString());
        assertTrue(emitter.events.get(0).stream().anyMatch(InsightProgress.class::isInstance));
        assertTrue(emitter.events.get(1).stream().anyMatch(value -> value.toString().contains("event:complete")));
        assertTrue(emitter.events.get(1).contains(result));
        assertTrue(emitter.completed);
    }

    @Test void runAnalysisEmitsGenericErrorWithoutLeakingProviderDetails() {
        when(analysis.analyze(any(), any())).thenThrow(new InsightProviderException("detalhe interno secreto"));
        var emitter = new CapturingEmitter();

        service.runAnalysis(job(), emitter);

        assertEquals(1, emitter.events.size());
        assertTrue(emitter.events.getFirst().stream().anyMatch(value -> value.toString().contains("event:analysis-error")), emitter.events.toString());
        var error = emitter.events.getFirst().stream().filter(Map.class::isInstance).map(Map.class::cast).findFirst().orElseThrow();
        assertEquals("Não foi possível concluir a análise agora. Tente novamente mais tarde.", error.get("message"));
        assertFalse(error.toString().contains("secreto"));
        assertTrue(emitter.completed);
    }

    private InsightJob job() {
        return new InsightJob(UUID.randomUUID(), userId, walletId,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    private static final class CapturingEmitter extends SseEmitter {
        private final List<List<Object>> events = new ArrayList<>();
        private boolean completed;
        @Override public void send(SseEventBuilder builder) throws IOException {
            events.add(builder.build().stream().map(item -> item.getData()).toList());
        }
        @Override public synchronized void complete() { completed = true; }
    }
}
