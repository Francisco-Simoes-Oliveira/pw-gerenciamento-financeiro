package com.financeiro.backend.features.transaction.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.category.repository.CategoryRepository;
import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.request.UpdateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.transaction.mapper.TransactionMapper;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.transaction.service.TransactionService;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.finance.service.FinancialService;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private WalletRepository walletRepository;
    
    @Autowired
    private WalletMemberRepository walletMemberRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionMapper mapper;
    
    @Autowired
    private FinancialService financialService;

    @Override
    public TransactionResponse insert(UUID currentUserId, CreateTransactionRequest request) {
        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + request.getWalletId()));

        checkEditPermission(wallet, currentUserId);
        
        Wallet destinationWallet = null;
        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getDestinationWalletId() == null) {
                throw new IllegalArgumentException("Carteira de destino é obrigatória para transferências.");
            }
            if (request.getWalletId().equals(request.getDestinationWalletId())) {
                throw new IllegalArgumentException("A carteira de origem e destino não podem ser as mesmas.");
            }
            destinationWallet = walletRepository.findById(request.getDestinationWalletId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carteira de destino não encontrada com ID: " + request.getDestinationWalletId()));
                    
            // Você precisa ter permissão de edição na origem. E na destino? Pode ser que não, mas por segurança exigiremos permissão.
            checkEditPermission(destinationWallet, currentUserId);
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + request.getCategoryId()));

        // Validação Categoria x TransactionType
        if (request.getType() == TransactionType.INCOME && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.INCOME) {
            throw new IllegalArgumentException("Receitas devem usar categorias do tipo INCOME.");
        }
        if (request.getType() == TransactionType.EXPENSE && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Despesas devem usar categorias do tipo EXPENSE.");
        }
        if (request.getType() == TransactionType.TRANSFER && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.TRANSFER) {
            throw new IllegalArgumentException("Transferências devem usar categorias do tipo TRANSFER.");
        }

        User createdBy = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário criador não encontrado."));

        Transaction transaction = mapper.toEntity(request);
        transaction.setWallet(wallet);
        transaction.setDestinationWallet(destinationWallet);
        transaction.setCategory(category);
        transaction.setCreatedBy(createdBy);
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction saved = repository.save(transaction);
        
        // Aplicar alterações financeiras
        if (saved.getType() == TransactionType.INCOME) {
            financialService.applyIncome(wallet, saved, createdBy);
        } else if (saved.getType() == TransactionType.EXPENSE) {
            financialService.applyExpense(wallet, saved, createdBy);
        } else if (saved.getType() == TransactionType.TRANSFER) {
            financialService.applyTransfer(wallet, destinationWallet, saved, createdBy);
        }
        
        return mapper.toResponse(saved);
    }

    @Override
    public List<TransactionResponse> listByWallet(UUID walletId, UUID currentUserId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada com ID: " + walletId));
                
        checkViewPermission(wallet, currentUserId);
        
        return repository.findAll().stream()
                .filter(t -> t.getWallet().getId().equals(walletId))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse searchById(UUID id, UUID currentUserId) {
        Transaction transaction = findEntityById(id);
        checkViewPermission(transaction.getWallet(), currentUserId);
        return mapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse alter(UUID id, UUID currentUserId, UpdateTransactionRequest request) {
        Transaction oldTransaction = findEntityById(id);
        
        checkEditPermission(oldTransaction.getWallet(), currentUserId);

        Category category = oldTransaction.getCategory();
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + request.getCategoryId()));
        }
        
        // Validar Categoria x TransactionType (novo)
        TransactionType newType = request.getType() != null ? request.getType() : oldTransaction.getType();
        if (newType == TransactionType.INCOME && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.INCOME) {
            throw new IllegalArgumentException("Receitas devem usar categorias do tipo INCOME.");
        }
        if (newType == TransactionType.EXPENSE && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.EXPENSE) {
            throw new IllegalArgumentException("Despesas devem usar categorias do tipo EXPENSE.");
        }
        if (newType == TransactionType.TRANSFER && category.getType() != com.financeiro.backend.features.category.enums.CategoryType.TRANSFER) {
            throw new IllegalArgumentException("Transferências devem usar categorias do tipo TRANSFER.");
        }

        // Criar clone da transação antiga antes de alterar para reversão
        Transaction oldTransactionClone = new Transaction();
        oldTransactionClone.setId(oldTransaction.getId());
        oldTransactionClone.setWallet(oldTransaction.getWallet());
        oldTransactionClone.setDestinationWallet(oldTransaction.getDestinationWallet());
        oldTransactionClone.setCategory(oldTransaction.getCategory());
        oldTransactionClone.setType(oldTransaction.getType());
        oldTransactionClone.setAmount(oldTransaction.getAmount());

        Transaction transaction = oldTransaction;
        transaction.setCategory(category);
        
        if (newType == TransactionType.TRANSFER) {
            if (request.getDestinationWalletId() != null) {
                if (transaction.getWallet().getId().equals(request.getDestinationWalletId())) {
                    throw new IllegalArgumentException("A carteira de origem e destino não podem ser as mesmas.");
                }
                Wallet dest = walletRepository.findById(request.getDestinationWalletId())
                        .orElseThrow(() -> new ResourceNotFoundException("Carteira destino não encontrada."));
                checkEditPermission(dest, currentUserId);
                transaction.setDestinationWallet(dest);
            } else if (transaction.getDestinationWallet() == null) {
                 throw new IllegalArgumentException("Transferências exigem uma carteira de destino.");
            }
        } else {
             transaction.setDestinationWallet(null);
        }

        mapper.updateEntityFromDto(request, transaction);
        transaction.setUpdatedAt(LocalDateTime.now());
        Transaction updated = repository.save(transaction);
        
        User updater = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        financialService.updateTransaction(oldTransactionClone, updated, updater);
        
        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void remove(UUID id, UUID currentUserId) {
        Transaction transaction = findEntityById(id);
        checkEditPermission(transaction.getWallet(), currentUserId);
        
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
                
        financialService.deleteTransaction(transaction, user);
        repository.delete(transaction);
    }

    private Transaction findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com o ID: " + id));
    }
    
    private void checkViewPermission(Wallet wallet, UUID currentUserId) {
        if (wallet.getOwner().getId().equals(currentUserId)) return;
        
        boolean isMember = walletMemberRepository.existsByWalletIdAndUserId(wallet.getId(), currentUserId);
        if (!isMember) {
            throw new SecurityException("Usuário não tem permissão para visualizar transações desta carteira.");
        }
    }
    
    private void checkEditPermission(Wallet wallet, UUID currentUserId) {
        if (wallet.getOwner().getId().equals(currentUserId)) return;
        
        WalletMember member = walletMemberRepository.findByWalletIdAndUserId(wallet.getId(), currentUserId)
                .orElseThrow(() -> new SecurityException("Usuário não tem permissão nesta carteira."));
                
        if (member.getPermission() != WalletPermission.EDITOR) {
            throw new SecurityException("Usuário não tem permissão de EDITOR para modificar transações.");
        }
    }
}
