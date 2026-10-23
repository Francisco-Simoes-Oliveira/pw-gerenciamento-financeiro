package com.financeiro.backend.features.gamification.event;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.financeiro.backend.features.gamification.enums.XpEventType;
import com.financeiro.backend.features.gamification.service.GamificationService;
import com.financeiro.backend.features.realtime.event.TransactionChangedEvent;
import com.financeiro.backend.features.reports.event.ReportViewedEvent;

class GamificationEventListenerTest {
    private final GamificationService service = org.mockito.Mockito.mock(GamificationService.class);
    private final GamificationEventListener listener = new GamificationEventListener(service);

    @Test
    void rewardsOnlyTransactionCreation() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        when(service.award(userId, XpEventType.FIRST_TRANSACTION, transactionId.toString()))
                .thenReturn(new GamificationService.AwardResult(true, 20, null));

        listener.onTransactionChanged(new TransactionChangedEvent(TransactionChangedEvent.ChangeType.CREATED,
                transactionId, Set.of(UUID.randomUUID()), userId, LocalDateTime.now()));
        listener.onTransactionChanged(new TransactionChangedEvent(TransactionChangedEvent.ChangeType.UPDATED,
                transactionId, Set.of(UUID.randomUUID()), userId, LocalDateTime.now()));

        verify(service).award(userId, XpEventType.FIRST_TRANSACTION, transactionId.toString());
        verify(service, never()).award(userId, XpEventType.FIRST_TRANSACTION, "");
        verify(service, never()).award(org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void aClosedPositiveMonthUnlocksTheMonthlyRewardWithAStableReference() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        listener.onReportViewed(new ReportViewedEvent(userId, walletId,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), new BigDecimal("640.00")));

        verify(service).award(userId, XpEventType.REPORT_VIEWED, walletId.toString());
        verify(service).award(userId, XpEventType.MONTH_POSITIVE, walletId + ":2026-08");
    }
}
