package com.financeiro.backend.features.reports.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.reports.projection.CategoryProjection;
import com.financeiro.backend.features.transaction.entity.Transaction;

@Repository
public interface CategoryReportRepository extends JpaRepository<Transaction, UUID> {

    @Query("""
        SELECT 
            c.name as categoryName,
            SUM(t.amount) as total
        FROM Transaction t 
        JOIN t.category c
        WHERE t.createdBy.id = :userId 
        AND t.type = 'EXPENSE'
        AND (:walletId IS NULL OR t.wallet.id = :walletId)
        GROUP BY c.id, c.name
        ORDER BY total DESC
    """)
    List<CategoryProjection> getExpensesByCategory(@Param("userId") UUID userId, @Param("walletId") UUID walletId);
}
