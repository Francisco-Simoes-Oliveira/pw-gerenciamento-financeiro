package com.financeiro.backend.features.finance.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.wallet.entity.Wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "financial_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne
    @JoinColumn(name = "transaction_id", nullable = true) // Pode ser nulo se for um recálculo geral, etc
    private Transaction transaction;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String operation; // EX: APPLY_INCOME, APPLY_EXPENSE, TRANSFER_OUT, TRANSFER_IN, REVERT_INCOME, RECALCULATE

    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private BigDecimal amount;

    private LocalDateTime createdAt;
}
