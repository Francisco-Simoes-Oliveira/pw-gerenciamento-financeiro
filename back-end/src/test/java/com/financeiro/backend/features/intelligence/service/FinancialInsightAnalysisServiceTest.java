package com.financeiro.backend.features.intelligence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeiro.backend.features.intelligence.dto.response.InsightResult;
import com.financeiro.backend.features.intelligence.exception.InsightProviderException;
import com.financeiro.backend.features.intelligence.model.FinancialInsightData;
import com.financeiro.backend.features.intelligence.model.InsightJob;
import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;

class FinancialInsightAnalysisServiceTest {
    private final UUID walletId = UUID.randomUUID(), userId = UUID.randomUUID();

    @Test void buildsOnlyAggregatesComparesPreviousPeriodAndReportsProgress() throws Exception {
        var reports = mock(StatementReportService.class);
        var current = report("Carteira pessoal secreta", LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31),
                new BigDecimal("5000"), new BigDecimal("1000"), List.of(
                        entry("Compra secreta", "Alimentação\nprincipal", "EXPENSE", TransactionStatus.PAID, "850"),
                        entry("Táxi", "Transporte", "EXPENSE", TransactionStatus.PAID, "150"),
                        entry("Ainda não pago", "Alimentação", "EXPENSE", TransactionStatus.PENDING, "200")));
        var previous = report("Carteira pessoal secreta", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30),
                new BigDecimal("4500"), new BigDecimal("800"), List.of(
                        entry("Mercado anterior", "alimentação PRINCIPAL", "EXPENSE", TransactionStatus.PAID, "700"),
                        entry("Ônibus", "Transporte", "EXPENSE", TransactionStatus.PAID, "100")));
        when(reports.generate(walletId, LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), userId)).thenReturn(current);
        when(reports.generate(walletId, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30), userId)).thenReturn(previous);

        var captured = new AtomicReference<FinancialInsightData>();
        FinancialInsightProvider provider = data -> {
            captured.set(data);
            return new InsightResult("Resumo", List.of("Destaque"), List.of("Recomendação"), "Aviso");
        };
        var service = new FinancialInsightAnalysisService(new FinancialInsightDataService(reports), provider);
        var progress = new ArrayList<String>();

        var result = service.analyze(job(), event -> progress.add(event.step()));

        assertEquals("Resumo", result.summary());
        assertEquals(List.of("PREPARING_DATA", "ANALYZING_CATEGORIES", "GENERATING_RECOMMENDATIONS"), progress);
        assertEquals(new BigDecimal("25.00"), captured.get().expenseChangePercent());
        assertEquals(2, captured.get().paidTransactionCount());
        assertEquals("Alimentação principal", captured.get().categories().getFirst().name());
        assertEquals(new BigDecimal("700"), captured.get().categories().getFirst().previousTotal());

        String providerPayload = new ObjectMapper().writeValueAsString(captured.get());
        assertFalse(providerPayload.contains("Carteira pessoal secreta"));
        assertFalse(providerPayload.contains("Compra secreta"));
        assertFalse(providerPayload.contains(walletId.toString()));
        assertFalse(providerPayload.contains(userId.toString()));
        assertFalse(providerPayload.contains("\\n"));
        verify(reports).generate(walletId, LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30), userId);
    }

    @Test void providerFailurePropagatesAfterAllProgressEvents() {
        var reports = mock(StatementReportService.class);
        when(reports.generate(any(), any(), any(), any())).thenReturn(report("Carteira", LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31), BigDecimal.ZERO, BigDecimal.ZERO, List.of()));
        FinancialInsightProvider provider = data -> { throw new InsightProviderException("falhou"); };
        var service = new FinancialInsightAnalysisService(new FinancialInsightDataService(reports), provider);
        var progress = new ArrayList<String>();

        assertThrows(InsightProviderException.class, () -> service.analyze(job(), event -> progress.add(event.step())));
        assertEquals(List.of("PREPARING_DATA", "ANALYZING_CATEGORIES", "GENERATING_RECOMMENDATIONS"), progress);
    }

    private InsightJob job() {
        return new InsightJob(UUID.randomUUID(), userId, walletId,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31));
    }

    private StatementReport report(String walletName, LocalDate start, LocalDate end,
            BigDecimal income, BigDecimal expense, List<StatementReport.Entry> entries) {
        return new StatementReport(walletId, walletName, "BRL", start, end, LocalDateTime.now(), income, expense,
                BigDecimal.ZERO, BigDecimal.ZERO, income.subtract(expense), 0, 0, entries);
    }

    private StatementReport.Entry entry(String title, String category, String movement,
            TransactionStatus status, String amount) {
        BigDecimal value = new BigDecimal(amount);
        return new StatementReport.Entry(UUID.randomUUID(), LocalDateTime.of(2026, 10, 5, 12, 0), title, category,
                movement, status, value, status == TransactionStatus.PAID ? value.negate() : BigDecimal.ZERO);
    }
}
