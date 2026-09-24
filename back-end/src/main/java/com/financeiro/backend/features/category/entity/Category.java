package com.financeiro.backend.features.category.entity;

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

import com.financeiro.backend.features.category.enums.CategoryType;
import com.financeiro.backend.features.wallet.entity.Wallet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Index;

@Entity
@Table(name = "category", indexes = {
    @Index(name = "idx_category_wallet_id", columnList = "wallet_id"),
    @Index(name = "idx_category_type", columnList = "type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = true) // Pode ser null se for categoria de sistema
    private Wallet wallet;

    private String name;
    private String icon;
    private String color;

    @Enumerated(EnumType.STRING)
    private CategoryType type;

    private Boolean active;
    
    private Boolean systemCategory;
}
