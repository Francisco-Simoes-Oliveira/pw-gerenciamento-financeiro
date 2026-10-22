package com.financeiro.backend.features.goals.event;

import java.util.UUID;

public record GoalCompletedEvent(UUID userId, UUID goalId) {
}
