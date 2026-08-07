package com.financeiro.backend.features.transaction.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.wallet.entity.Wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Index;

@Entity
@Table(name = "transaction", indexes = {
    @Index(name = "idx_transaction_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_transaction_category_id", columnList = "category_id"),
    @Index(name = "idx_transaction_created_by", columnList = "created_by"),
    @Index(name = "idx_transaction_type", columnList = "type"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    
    @ManyToOne
    @JoinColumn(name = "destination_wallet_id", nullable = true)
    private Wallet destinationWallet;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private String title;
    private String description;
    private java.math.BigDecimal amount;
    
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    
    private String attachmentUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
