package com.financeiro.backend.features.gamification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.gamification.enums.XpEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "xp_event",
        uniqueConstraints = @UniqueConstraint(name = "uk_xp_event_user_key", columnNames = {"user_id", "idempotency_key"}),
        indexes = @Index(name = "idx_xp_event_user_date", columnList = "user_id,occurred_at"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XpEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private XpEventType type;

    @Column(nullable = false)
    private int xp;

    @Column(name = "idempotency_key", nullable = false, length = 120)
    private String idempotencyKey;

    @Column(name = "reference_id", length = 80)
    private String referenceId;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}
