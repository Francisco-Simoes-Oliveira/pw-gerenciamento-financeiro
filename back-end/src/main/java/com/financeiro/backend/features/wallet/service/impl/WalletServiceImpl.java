package com.financeiro.backend.features.wallet.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.wallet.dto.request.CreateWalletRequest;
import com.financeiro.backend.features.wallet.dto.request.UpdateWalletRequest;
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
    private WalletMapper mapper;

    @Override
    public WalletResponse insert(UUID ownerId, CreateWalletRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner não encontrado com ID: " + ownerId));

        Wallet wallet = mapper.toEntity(request);
        wallet.setOwner(owner);
        wallet.setActive(true);
        wallet.setCreatedAt(LocalDateTime.now());
        
        Wallet saved = repository.save(wallet);
        return mapper.toResponse(saved);
    }

    @Override
    public List<WalletResponse> listByOwner(UUID ownerId) {
        // Seria ideal ter um método findByOwnerId no repository, por simplicidade vou listar todas (apenas para o teste/exemplo, depois refatorar)
        // Adicionando um filter provisório:
        return repository.findAll().stream()
                .filter(w -> w.getOwner().getId().equals(ownerId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WalletResponse searchById(UUID id) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
        return mapper.toResponse(wallet);
    }

    @Override
    public WalletResponse alter(UUID id, UpdateWalletRequest request) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
        
        mapper.updateEntityFromDto(request, wallet);
        Wallet updated = repository.save(wallet);
        return mapper.toResponse(updated);
    }

    @Override
    public void remove(UUID id) {
        Wallet wallet = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + id));
        repository.delete(wallet);
    }

    @Override
    public void addMember(UUID walletId, UUID userId, String permission) {
        Wallet wallet = repository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        WalletMember member = WalletMember.builder()
                .wallet(wallet)
                .user(user)
                .permission(WalletPermission.valueOf(permission.toUpperCase()))
                .joinedAt(LocalDateTime.now())
                .build();
                
        memberRepository.save(member);
    }

    @Override
    public void removeMember(UUID walletId, UUID userId) {
        List<WalletMember> members = memberRepository.findByWalletId(walletId);
        WalletMember toRemove = members.stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Membro não encontrado na carteira"));
        
        memberRepository.delete(toRemove);
    }
}
