package com.financeiro.backend.features.gamification.enums;

import java.util.Arrays;
import java.util.Optional;

public enum AchievementCode {
    FIRST_STEP("Primeiro Passo", "Registre sua primeira transação.", XpEventType.FIRST_TRANSACTION),
    IN_THE_BLACK("No Azul", "Finalize um mês com saldo realizado positivo.", XpEventType.MONTH_POSITIVE),
    TEAM_FINANCES("Finanças em Equipe", "Compartilhe sua primeira carteira.", XpEventType.WALLET_SHARED),
    GOAL_ACHIEVED("Objetivo Cumprido", "Conclua sua primeira meta financeira.", XpEventType.GOAL_COMPLETED),
    FINANCIAL_ANALYST("Analista Financeiro", "Consulte seu primeiro relatório mensal.", XpEventType.REPORT_VIEWED),
    WATCHING_SPENDING("Olho nos Gastos", "Use o Resumo Inteligente pela primeira vez.", XpEventType.AI_INSIGHT_USED);

    private final String title;
    private final String description;
    private final XpEventType trigger;

    AchievementCode(String title, String description, XpEventType trigger) {
        this.title = title;
        this.description = description;
        this.trigger = trigger;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public XpEventType getTrigger() {
        return trigger;
    }

    public static Optional<AchievementCode> forEvent(XpEventType eventType) {
        return Arrays.stream(values()).filter(value -> value.trigger == eventType).findFirst();
    }
}
