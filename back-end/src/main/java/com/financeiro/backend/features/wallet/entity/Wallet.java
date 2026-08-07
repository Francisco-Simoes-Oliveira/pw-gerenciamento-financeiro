package com.financeiro.backend.features.wallet.entity;

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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Index;

@Entity
@Table(name = "wallet", indexes = {
    @Index(name = "idx_wallet_owner_id", columnList = "owner_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    private String name;
    private String description;
    private String currency;
    private Boolean active;
    private LocalDateTime createdAt;
    private String color;
    private String icon;

    @Builder.Default
    private java.math.BigDecimal balance = java.math.BigDecimal.ZERO;
    private LocalDateTime lastBalanceUpdate;
}
