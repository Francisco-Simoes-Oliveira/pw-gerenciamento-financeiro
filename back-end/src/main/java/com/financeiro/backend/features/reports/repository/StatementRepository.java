package com.financeiro.backend.features.reports.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.transaction.entity.Transaction;

@Repository
public interface StatementRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    // Queries via Specifications para suportar paginação e múltiplos filtros
}
