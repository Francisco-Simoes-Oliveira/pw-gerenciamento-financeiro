package com.financeiro.backend.features.reports.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.reports.projection.DashboardProjection;
import com.financeiro.backend.features.reports.projection.MonthlyProjection;
import com.financeiro.backend.features.transaction.entity.Transaction;

@Repository
public interface DashboardRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT 
            SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) as income,
            SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as expense,
            SUM(CASE WHEN t.type = 'TRANSFER' THEN t.amount ELSE 0 END) as transfer,
            COUNT(t.id) as transactionsCount
        FROM Transaction t 
        WHERE t.createdBy.id = :userId 
        AND (:walletId IS NULL OR t.wallet.id = :walletId)
    """)
    DashboardProjection getDashboardConsolidated(@Param("userId") UUID userId, @Param("walletId") UUID walletId);

    @Query(value = """
        SELECT 
            YEAR(t.created_at) as year,
            MONTH(t.created_at) as month,
            SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END) as income,
            SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END) as expense
        FROM transaction t 
        WHERE t.created_by = :userId 
        AND (:walletId IS NULL OR t.wallet_id = :walletId)
        GROUP BY YEAR(t.created_at), MONTH(t.created_at)
        ORDER BY year DESC, month DESC
        LIMIT 12
    """, nativeQuery = true)
    List<MonthlyProjection> getMonthlyBalance(@Param("userId") UUID userId, @Param("walletId") UUID walletId);
}
