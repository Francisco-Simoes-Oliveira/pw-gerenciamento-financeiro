package com.financeiro.backend.features.gamification.enums;

public enum XpEventType {
    FIRST_TRANSACTION(20, "Primeira transação", true),
    FIRST_WALLET(30, "Primeira carteira", true),
    WALLET_SHARED(40, "Primeira carteira compartilhada", true),
    GOAL_CREATED(30, "Primeira meta criada", true),
    GOAL_COMPLETED(200, "Meta concluída", false),
    MONTH_POSITIVE(150, "Mês fechado no azul", false),
    REPORT_VIEWED(20, "Primeiro relatório consultado", true),
    AI_INSIGHT_USED(50, "Primeiro Resumo Inteligente", true);

    private final int xp;
    private final String label;
    private final boolean oneTime;

    XpEventType(int xp, String label, boolean oneTime) {
        this.xp = xp;
        this.label = label;
        this.oneTime = oneTime;
    }

    public int getXp() {
        return xp;
    }

    public String getLabel() {
        return label;
    }

    public boolean isOneTime() {
        return oneTime;
    }
}
