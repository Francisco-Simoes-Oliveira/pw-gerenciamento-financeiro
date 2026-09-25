package com.financeiro.backend.features.reports.repository;

import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.transaction.entity.Transaction;

@Repository
public interface StatementRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    @Query("""
        select t from Transaction t
        join fetch t.wallet w
        left join fetch t.destinationWallet d
        left join fetch t.category
        where (w.id = :walletId or (t.type = 'TRANSFER' and d.id = :walletId))
          and coalesce(t.transactionDate, t.createdAt) >= :start
          and coalesce(t.transactionDate, t.createdAt) < :endExclusive
        order by coalesce(t.transactionDate, t.createdAt), t.id
        """)
    List<Transaction> findForStatement(@Param("walletId") UUID walletId,
            @Param("start") LocalDateTime start, @Param("endExclusive") LocalDateTime endExclusive,
            Pageable pageable);
}
