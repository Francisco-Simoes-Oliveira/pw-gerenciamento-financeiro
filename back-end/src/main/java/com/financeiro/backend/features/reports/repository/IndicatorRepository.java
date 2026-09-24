package com.financeiro.backend.features.reports.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.reports.projection.IndicatorProjection;
import com.financeiro.backend.features.transaction.entity.Transaction;

@Repository
public interface IndicatorRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT 
            MAX(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) as maxIncome,
            MAX(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as maxExpense,
            COUNT(t.id) as transactionCount,
            COUNT(DISTINCT t.category.id) as categoryCount
        FROM Transaction t 
        WHERE t.createdBy.id = :userId 
        AND (:walletId IS NULL OR t.wallet.id = :walletId)
    """)
    IndicatorProjection getIndicators(@Param("userId") UUID userId, @Param("walletId") UUID walletId);
}
