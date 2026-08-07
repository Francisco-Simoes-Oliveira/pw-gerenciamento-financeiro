package com.financeiro.backend.features.finance.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.finance.entity.FinancialAudit;

@Repository
public interface FinancialAuditRepository extends JpaRepository<FinancialAudit, UUID> {
}
