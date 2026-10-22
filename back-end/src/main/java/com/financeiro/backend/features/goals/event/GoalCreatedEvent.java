package com.financeiro.backend.features.goals.event;

import java.util.UUID;

public record GoalCreatedEvent(UUID userId, UUID goalId) {
}
