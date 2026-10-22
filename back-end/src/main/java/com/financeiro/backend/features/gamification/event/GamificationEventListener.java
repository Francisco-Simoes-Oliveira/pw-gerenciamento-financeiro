package com.financeiro.backend.features.gamification.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.financeiro.backend.features.gamification.enums.XpEventType;
import com.financeiro.backend.features.gamification.service.GamificationService;
import com.financeiro.backend.features.goals.event.GoalCompletedEvent;
import com.financeiro.backend.features.goals.event.GoalCreatedEvent;
import com.financeiro.backend.features.intelligence.event.InsightStartedEvent;
import com.financeiro.backend.features.realtime.event.TransactionChangedEvent;
import com.financeiro.backend.features.reports.event.ReportViewedEvent;
import com.financeiro.backend.features.wallet.event.WalletCreatedEvent;
import com.financeiro.backend.features.wallet.event.WalletSharedEvent;

@Component
public class GamificationEventListener {
    private static final Logger log = LoggerFactory.getLogger(GamificationEventListener.class);
    private final GamificationService gamification;

    public GamificationEventListener(GamificationService gamification) {
        this.gamification = gamification;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTransactionChanged(TransactionChangedEvent event) {
        if (event.type() == TransactionChangedEvent.ChangeType.CREATED) {
            safelyAward(event.changedByUserId(), XpEventType.FIRST_TRANSACTION, event.transactionId().toString());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWalletCreated(WalletCreatedEvent event) {
        safelyAward(event.userId(), XpEventType.FIRST_WALLET, event.walletId().toString());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onWalletShared(WalletSharedEvent event) {
        safelyAward(event.userId(), XpEventType.WALLET_SHARED, event.walletId().toString());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReportViewed(ReportViewedEvent event) {
        safelyAward(event.userId(), XpEventType.REPORT_VIEWED, event.walletId().toString());
        if (event.isClosedPositiveMonth()) {
            safelyAward(event.userId(), XpEventType.MONTH_POSITIVE, event.monthReference());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onInsightStarted(InsightStartedEvent event) {
        safelyAward(event.userId(), XpEventType.AI_INSIGHT_USED, event.analysisId().toString());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onGoalCreated(GoalCreatedEvent event) {
        safelyAward(event.userId(), XpEventType.GOAL_CREATED, event.goalId().toString());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onGoalCompleted(GoalCompletedEvent event) {
        safelyAward(event.userId(), XpEventType.GOAL_COMPLETED, event.goalId().toString());
    }

    private void safelyAward(java.util.UUID userId, XpEventType type, String referenceId) {
        try {
            gamification.award(userId, type, referenceId);
        } catch (RuntimeException exception) {
            log.error("Não foi possível registrar o evento de gamificação {} para o usuário {}.",
                    type, userId, exception);
        }
    }
}
