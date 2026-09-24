package com.financeiro.backend.features.wallet.service;

import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;

@Service
public class WalletAccessService {

    private final WalletMemberRepository memberRepository;

    public WalletAccessService(WalletMemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public void requireView(Wallet wallet, UUID userId) {
        if (isOwner(wallet, userId)) {
            return;
        }

        if (!memberRepository.existsByWalletIdAndUserId(wallet.getId(), userId)) {
            throw new AccessDeniedException("Usuário não tem permissão para visualizar esta carteira.");
        }
    }

    public void requireTransactionEdit(Wallet wallet, UUID userId) {
        if (isOwner(wallet, userId)) {
            return;
        }

        WalletMember member = memberRepository.findByWalletIdAndUserId(wallet.getId(), userId)
                .orElseThrow(() -> new AccessDeniedException("Usuário não tem permissão nesta carteira."));

        if (member.getPermission() != WalletPermission.EDITOR) {
            throw new AccessDeniedException("Usuário precisa ser EDITOR para modificar transações desta carteira.");
        }
    }

    public void requireOwner(Wallet wallet, UUID userId) {
        if (!isOwner(wallet, userId)) {
            throw new AccessDeniedException("Apenas o OWNER pode realizar esta operação.");
        }
    }

    public boolean isOwner(Wallet wallet, UUID userId) {
        return wallet != null
                && wallet.getOwner() != null
                && wallet.getOwner().getId() != null
                && wallet.getOwner().getId().equals(userId);
    }
}
