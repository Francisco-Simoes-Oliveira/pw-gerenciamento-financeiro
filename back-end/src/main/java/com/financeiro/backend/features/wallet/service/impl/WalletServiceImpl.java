package com.financeiro.backend.features.wallet.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.common.exception.ConflictException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.AddWalletMemberRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletMemberRequest;
import com.financeiro.backend.features.wallet.dto.response.WalletMemberResponse;
import com.financeiro.backend.features.wallet.dto.response.WalletResponse;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.mapper.WalletMapper;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
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

    @Override
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
        return repository.findAll().stream()
            .filter(w -> w.getOwner().getId().equals(ownerId)
                || memberRepository.existsByWalletIdAndUserId(w.getId(), ownerId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponse searchById(UUID id, UUID currentUserId) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
                
        checkViewPermission(wallet, currentUserId);
        return mapper.toResponse(wallet);
    }

    @Override
    public WalletResponse alter(UUID id, UUID currentUserId, UpdateWalletRequest request) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
        
        checkEditPermission(wallet, currentUserId);
        
        mapper.updateEntityFromDto(request, wallet);
        Wallet updated = repository.save(wallet);
        return mapper.toResponse(updated);
    }

    @Override
    public void remove(UUID id, UUID currentUserId) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
        
        if (!wallet.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Apenas o OWNER pode excluir a carteira.");
        }
        
        if (transactionRepository.existsByWalletId(id)) {
            throw new IllegalArgumentException("Não é possível excluir uma carteira que possui transações associadas.");
        }
        
        repository.delete(wallet);
    }

    @Override
    public void addMember(UUID walletId, UUID currentUserId, UUID targetUserId, String permission) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
        
        if (!wallet.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Apenas o OWNER pode adicionar membros.");
        }
        
        if (memberRepository.existsByWalletIdAndUserId(walletId, targetUserId)) {
            throw new ConflictException("O usuário já é membro desta carteira.");
        }
        
        UserSubscription subscription = subscriptionRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalStateException("O proprietário precisa de uma assinatura ativa para adicionar membros."));
                
        Integer maxMembers = subscription.getPlan().getMaxMembersPerWallet();
        if (maxMembers != null) {
            long currentMembers = memberRepository.countByWalletId(walletId);
            if (currentMembers >= maxMembers) {
                throw new IllegalArgumentException("Limite de membros do plano atingido.");
            }
        }
        
        User user = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + targetUserId));

        WalletMember member = WalletMember.builder()
                .wallet(wallet)
                .user(user)
                .permission(WalletPermission.valueOf(permission.toUpperCase()))
                .joinedAt(LocalDateTime.now())
                .build();
                
        memberRepository.save(member);
    }

    @Override
    public List<WalletMemberResponse> listMembers(UUID walletId, UUID currentUserId) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
        checkViewPermission(wallet, currentUserId);

        List<WalletMemberResponse> members = new java.util.ArrayList<>();
        members.add(toMemberResponse(wallet.getOwner(), WalletPermission.OWNER));
        members.addAll(memberRepository.findByWalletId(walletId).stream()
                .map(member -> toMemberResponse(member.getUser(), member.getPermission()))
                .collect(Collectors.toList()));
        return members;
    }

    @Override
    public void addMemberByEmail(UUID walletId, UUID currentUserId, AddWalletMemberRequest request) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
        requireOwner(wallet, currentUserId);

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (wallet.getOwner().getId().equals(targetUser.getId())
                || memberRepository.existsByWalletIdAndUserId(walletId, targetUser.getId())) {
            throw new ConflictException("O usuário já é membro desta carteira.");
        }
        addMember(walletId, currentUserId, targetUser.getId(), request.getRole().name());
    }

    @Override
    public void updateMember(UUID walletId, UUID currentUserId, UUID targetUserId, UpdateWalletMemberRequest request) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
        requireOwner(wallet, currentUserId);
        if (wallet.getOwner().getId().equals(targetUserId)) {
            throw new IllegalArgumentException("O papel do OWNER não pode ser alterado.");
        }

        WalletMember member = memberRepository.findByWalletIdAndUserId(walletId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado na carteira"));
        member.setPermission(request.getRole());
        memberRepository.save(member);
    }

    @Override
    public void removeMember(UUID walletId, UUID currentUserId, UUID targetUserId) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
                
        if (!wallet.getOwner().getId().equals(currentUserId) && !currentUserId.equals(targetUserId)) {
            throw new SecurityException("Apenas o OWNER pode remover membros (ou o próprio membro pode sair).");
        }

        WalletMember toRemove = memberRepository.findByWalletIdAndUserId(walletId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado na carteira"));
        
        memberRepository.delete(toRemove);
    }
    
    private void checkViewPermission(Wallet wallet, UUID currentUserId) {
        if (wallet.getOwner().getId().equals(currentUserId)) return;
        
        boolean isMember = memberRepository.existsByWalletIdAndUserId(wallet.getId(), currentUserId);
        if (!isMember) {
            throw new SecurityException("Usuário não tem permissão para visualizar esta carteira.");
        }
    }
    
    private void checkEditPermission(Wallet wallet, UUID currentUserId) {
        if (wallet.getOwner().getId().equals(currentUserId)) return;
        
        WalletMember member = memberRepository.findByWalletIdAndUserId(wallet.getId(), currentUserId)
                .orElseThrow(() -> new SecurityException("Usuário não tem permissão para editar esta carteira."));
                
        if (member.getPermission() != WalletPermission.EDITOR) {
            throw new SecurityException("Usuário não tem permissão de EDITOR para editar esta carteira.");
        }
    }

    private void requireOwner(Wallet wallet, UUID currentUserId) {
        if (!wallet.getOwner().getId().equals(currentUserId)) {
            throw new SecurityException("Apenas o OWNER pode gerenciar membros.");
        }
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
