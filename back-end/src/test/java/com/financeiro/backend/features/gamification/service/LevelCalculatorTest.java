package com.financeiro.backend.features.gamification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class LevelCalculatorTest {
    private final LevelCalculator calculator = new LevelCalculator();

    @Test
    void calculatesLevelBoundariesAndProgress() {
        var initial = calculator.calculate(0);
        assertEquals(1, initial.level());
        assertEquals(0, initial.currentLevelXp());
        assertEquals(200, initial.nextLevelTotalXp());

        var almostNext = calculator.calculate(199);
        assertEquals(1, almostNext.level());
        assertEquals(199, almostNext.currentLevelXp());
        assertEquals(99, almostNext.progressPercentage());

        var next = calculator.calculate(200);
        assertEquals(2, next.level());
        assertEquals(0, next.currentLevelXp());
        assertEquals(400, next.nextLevelTotalXp());
        assertEquals("Organizador Financeiro", next.rank());
    }
}
