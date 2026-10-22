package com.financeiro.backend.features.gamification.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.backend.features.gamification.entity.XpEvent;

public interface XpEventRepository extends JpaRepository<XpEvent, UUID> {
    boolean existsByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
    List<XpEvent> findTop10ByUserIdOrderByOccurredAtDesc(UUID userId);
}
