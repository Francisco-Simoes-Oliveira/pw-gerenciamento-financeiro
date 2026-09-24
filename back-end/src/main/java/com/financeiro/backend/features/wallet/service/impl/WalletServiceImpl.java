package com.financeiro.backend.features.wallet.service.impl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ConflictException;
import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.dto.request.AddWalletMemberRequest;
import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletMemberRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletMemberResponse;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.mapper.WalletMapper;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;
import com.financeiro.backend.features.wallet.service.WalletService;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository repository;

    @Autowired
    private WalletMemberRepository memberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserSubscriptionRepository subscriptionRepository;

    @Autowired
    private WalletMapper mapper;

    @Autowired
    private WalletAccessService walletAccessService;

    @Override
    @Transactional
    public WalletResponse insert(UUID ownerId, CreateWalletRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner não encontrado com ID: " + ownerId));

        UserSubscription subscription = subscriptionRepository.findByUserId(ownerId)
                .orElseThrow(() -> new IllegalStateException("O usuário precisa de uma assinatura ativa para criar carteiras."));

        Integer maxWallets = subscription.getPlan().getMaxWallets();
        if (maxWallets != null) {
            long currentWallets = repository.countByOwnerId(ownerId);
            if (currentWallets >= maxWallets) {
                throw new IllegalArgumentException("Limite de carteiras do plano atingido.");
            }
        }

        Wallet wallet = mapper.toEntity(request);
        wallet.setOwner(owner);
        wallet.setActive(true);
        wallet.setCreatedAt(LocalDateTime.now());

        Wallet saved = repository.save(wallet);
        return mapper.toResponse(saved);
    }

    @Override
    public List<WalletResponse> listByOwner(UUID ownerId) {
        LinkedHashMap<UUID, Wallet> accessibleWallets = new LinkedHashMap<>();

        repository.findByOwnerId(ownerId)
                .forEach(wallet -> accessibleWallets.put(wallet.getId(), wallet));

        memberRepository.findByUserId(ownerId).stream()
                .map(WalletMember::getWallet)
                .forEach(wallet -> accessibleWallets.putIfAbsent(wallet.getId(), wallet));

        return accessibleWallets.values().stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public WalletResponse searchById(UUID id, UUID currentUserId) {
        Wallet wallet = findWallet(id);
        walletAccessService.requireView(wallet, currentUserId);
        return mapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse alter(UUID id, UUID currentUserId, UpdateWalletRequest request) {
        Wallet wallet = findWallet(id);

        // A especificação reserva alterações dos dados da carteira ao OWNER.
        walletAccessService.requireOwner(wallet, currentUserId);

        mapper.updateEntityFromDto(request, wallet);
        Wallet updated = repository.save(wallet);
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void remove(UUID id, UUID currentUserId) {
        Wallet wallet = findWallet(id);
        walletAccessService.requireOwner(wallet, currentUserId);

        if (transactionRepository.existsByWalletId(id)) {
            throw new IllegalArgumentException("Não é possível excluir uma carteira que possui transações associadas.");
        }

        repository.delete(wallet);
    }

    @Override
    @Transactional
    public void addMember(UUID walletId, UUID currentUserId, UUID targetUserId, String permission) {
        Wallet wallet = findWallet(walletId);
        walletAccessService.requireOwner(wallet, currentUserId);

        if (wallet.getOwner().getId().equals(targetUserId)) {
            throw new ConflictException("O OWNER já pertence à carteira.");
        }

        WalletPermission walletPermission = parseAssignablePermission(permission);

        if (memberRepository.existsByWalletIdAndUserId(walletId, targetUserId)) {
            throw new ConflictException("O usuário já é membro desta carteira.");
        }

        validateMemberLimit(wallet, currentUserId);

        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + targetUserId));

        WalletMember member = WalletMember.builder()
                .wallet(wallet)
                .user(user)
                .permission(walletPermission)
                .joinedAt(LocalDateTime.now())
                .build();

        memberRepository.save(member);
    }

    @Override
    public List<WalletMemberResponse> listMembers(UUID walletId, UUID currentUserId) {
        Wallet wallet = findWallet(walletId);
        walletAccessService.requireView(wallet, currentUserId);

        List<WalletMemberResponse> members = new java.util.ArrayList<>();
        members.add(toMemberResponse(wallet.getOwner(), WalletPermission.OWNER));
        members.addAll(memberRepository.findByWalletId(walletId).stream()
                .map(member -> toMemberResponse(member.getUser(), member.getPermission()))
                .toList());
        return members;
    }

    @Override
    @Transactional
    public void addMemberByEmail(UUID walletId, UUID currentUserId, AddWalletMemberRequest request) {
        Wallet wallet = findWallet(walletId);
        walletAccessService.requireOwner(wallet, currentUserId);
        validateAssignablePermission(request.getRole());

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        if (wallet.getOwner().getId().equals(targetUser.getId())
                || memberRepository.existsByWalletIdAndUserId(walletId, targetUser.getId())) {
            throw new ConflictException("O usuário já é membro desta carteira.");
        }

        validateMemberLimit(wallet, currentUserId);

        WalletMember member = WalletMember.builder()
                .wallet(wallet)
                .user(targetUser)
                .permission(request.getRole())
                .joinedAt(LocalDateTime.now())
                .build();
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void updateMember(UUID walletId, UUID currentUserId, UUID targetUserId, UpdateWalletMemberRequest request) {
        Wallet wallet = findWallet(walletId);
        walletAccessService.requireOwner(wallet, currentUserId);

        if (wallet.getOwner().getId().equals(targetUserId)) {
            throw new IllegalArgumentException("O papel do OWNER não pode ser alterado.");
        }

        validateAssignablePermission(request.getRole());

        WalletMember member = memberRepository.findByWalletIdAndUserId(walletId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado na carteira"));
        member.setPermission(request.getRole());
        memberRepository.save(member);
    }

    @Override
    @Transactional
    public void removeMember(UUID walletId, UUID currentUserId, UUID targetUserId) {
        Wallet wallet = findWallet(walletId);

        if (wallet.getOwner().getId().equals(targetUserId)) {
            throw new IllegalArgumentException("O OWNER não pode ser removido da própria carteira.");
        }

        boolean ownerRemovingMember = walletAccessService.isOwner(wallet, currentUserId);
        boolean memberLeaving = currentUserId.equals(targetUserId);

        if (!ownerRemovingMember && !memberLeaving) {
            throw new AccessDeniedException("Apenas o OWNER pode remover outro membro da carteira.");
        }

        WalletMember toRemove = memberRepository.findByWalletIdAndUserId(walletId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado na carteira"));

        memberRepository.delete(toRemove);
    }

    private void validateMemberLimit(Wallet wallet, UUID ownerId) {
        UserSubscription subscription = subscriptionRepository.findByUserId(ownerId)
                .orElseThrow(() -> new IllegalStateException("O proprietário precisa de uma assinatura ativa para adicionar membros."));

        Integer maxMembers = subscription.getPlan().getMaxMembersPerWallet();
        if (maxMembers != null) {
            long currentMembers = memberRepository.countByWalletId(wallet.getId());
            if (currentMembers >= maxMembers) {
                throw new IllegalArgumentException("Limite de membros do plano atingido.");
            }
        }
    }

    private WalletPermission parseAssignablePermission(String permission) {
        if (permission == null || permission.isBlank()) {
            throw new IllegalArgumentException("Permissão é obrigatória.");
        }

        WalletPermission parsed;
        try {
            parsed = WalletPermission.valueOf(permission.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Permissão inválida. Use EDITOR ou VIEWER.");
        }

        validateAssignablePermission(parsed);
        return parsed;
    }

    private void validateAssignablePermission(WalletPermission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Permissão é obrigatória.");
        }
        if (permission == WalletPermission.OWNER) {
            throw new IllegalArgumentException("A permissão OWNER é reservada ao proprietário da carteira.");
        }
    }

    private Wallet findWallet(UUID walletId) {
        return repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
    }

    private WalletMemberResponse toMemberResponse(User user, WalletPermission permission) {
        return WalletMemberResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(permission)
                .build();
    }
}
