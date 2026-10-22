package com.financeiro.backend.features.goals.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financeiro.backend.features.goals.entity.FinancialGoal;

public interface FinancialGoalRepository extends JpaRepository<FinancialGoal, UUID> {
    List<FinancialGoal> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<FinancialGoal> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByWalletId(UUID walletId);
}
