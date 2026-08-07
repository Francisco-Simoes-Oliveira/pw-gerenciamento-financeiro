package com.financeiro.backend.features.wallet.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.financeiro.backend.features.wallet.entity.WalletMember;

@Repository
public interface WalletMemberRepository extends JpaRepository<WalletMember, UUID> {
    List<WalletMember> findByWalletId(UUID walletId);
    boolean existsByWalletIdAndUserId(UUID walletId, UUID userId);
    java.util.Optional<WalletMember> findByWalletIdAndUserId(UUID walletId, UUID userId);
    long countByWalletId(UUID walletId);
}
