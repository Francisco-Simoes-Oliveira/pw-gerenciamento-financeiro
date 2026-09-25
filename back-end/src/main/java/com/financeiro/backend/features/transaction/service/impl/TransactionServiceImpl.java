package com.financeiro.backend.features.transaction.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.category.enums.CategoryType;
import com.financeiro.backend.features.category.repository.CategoryRepository;
import com.financeiro.backend.features.finance.service.FinancialService;
import com.financeiro.backend.features.realtime.event.TransactionChangedEvent;
import com.financeiro.backend.features.realtime.event.TransactionChangedEvent.ChangeType;
import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.request.UpdateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.transaction.mapper.TransactionMapper;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.transaction.service.TransactionService;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository repository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionMapper mapper;

    @Autowired
    private FinancialService financialService;

    @Autowired
    private WalletAccessService walletAccessService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public TransactionResponse insert(UUID currentUserId, CreateTransactionRequest request) {
        Wallet wallet = findWallet(request.getWalletId(), "Carteira não encontrada com ID: ");
        walletAccessService.requireTransactionEdit(wallet, currentUserId);

        Wallet destinationWallet = resolveDestinationWalletForCreate(request, currentUserId, wallet);
        Category category = findCategory(request.getCategoryId());
        validateCategoryForWallet(category, wallet);
        validateCategoryType(category, request.getType());

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
        applyFinancialImpact(saved, createdBy);
        publishTransactionChange(ChangeType.CREATED, saved, currentUserId);

        return mapper.toResponse(saved);
    }

    @Override
    public List<TransactionResponse> listByWallet(UUID walletId, UUID currentUserId) {
        Wallet wallet = findWallet(walletId, "Carteira não encontrada com ID: ");
        walletAccessService.requireView(wallet, currentUserId);

        return repository.findByWalletIdOrderByTransactionDateDesc(walletId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public TransactionResponse searchById(UUID id, UUID currentUserId) {
        Transaction transaction = findEntityById(id);
        walletAccessService.requireView(transaction.getWallet(), currentUserId);
        return mapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse alter(UUID id, UUID currentUserId, UpdateTransactionRequest request) {
        Transaction transaction = findEntityById(id);
        walletAccessService.requireTransactionEdit(transaction.getWallet(), currentUserId);

        // Uma transferência altera duas carteiras. Para editar/reverter com segurança,
        // o usuário também precisa continuar com permissão de edição no destino antigo.
        if (transaction.getType() == TransactionType.TRANSFER && transaction.getDestinationWallet() != null) {
            walletAccessService.requireTransactionEdit(transaction.getDestinationWallet(), currentUserId);
        }

        Transaction oldTransactionSnapshot = snapshotForFinancialReversal(transaction);

        TransactionType newType = request.getType() != null ? request.getType() : transaction.getType();
        Category category = request.getCategoryId() != null
                ? findCategory(request.getCategoryId())
                : transaction.getCategory();

        validateCategoryForWallet(category, transaction.getWallet());
        validateCategoryType(category, newType);

        Wallet destinationWallet = resolveDestinationWalletForUpdate(
                transaction,
                request,
                newType,
                currentUserId
        );

        mapper.updateEntityFromDto(request, transaction);
        transaction.setCategory(category);
        transaction.setDestinationWallet(destinationWallet);
        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = repository.save(transaction);

        User updater = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        financialService.updateTransaction(oldTransactionSnapshot, updated, updater);
        publishTransactionChange(
                ChangeType.UPDATED,
                updated,
                currentUserId,
                oldTransactionSnapshot.getDestinationWallet()
        );

        return mapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void remove(UUID id, UUID currentUserId) {
        Transaction transaction = findEntityById(id);
        walletAccessService.requireTransactionEdit(transaction.getWallet(), currentUserId);

        if (transaction.getType() == TransactionType.TRANSFER && transaction.getDestinationWallet() != null) {
            walletAccessService.requireTransactionEdit(transaction.getDestinationWallet(), currentUserId);
        }

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        financialService.deleteTransaction(transaction, user);
        repository.delete(transaction);
        publishTransactionChange(ChangeType.DELETED, transaction, currentUserId);
    }

    private void publishTransactionChange(
            ChangeType type,
            Transaction transaction,
            UUID currentUserId,
            Wallet... additionalWallets
    ) {
        Set<UUID> walletIds = new HashSet<>();
        addWalletId(walletIds, transaction.getWallet());
        addWalletId(walletIds, transaction.getDestinationWallet());

        if (additionalWallets != null) {
            for (Wallet wallet : additionalWallets) {
                addWalletId(walletIds, wallet);
            }
        }

        eventPublisher.publishEvent(new TransactionChangedEvent(
                type,
                transaction.getId(),
                walletIds,
                currentUserId,
                LocalDateTime.now()
        ));
    }

    private void addWalletId(Set<UUID> walletIds, Wallet wallet) {
        if (wallet != null && wallet.getId() != null) {
            walletIds.add(wallet.getId());
        }
    }

    private Wallet resolveDestinationWalletForCreate(
            CreateTransactionRequest request,
            UUID currentUserId,
            Wallet originWallet
    ) {
        if (request.getType() != TransactionType.TRANSFER) {
            if (request.getDestinationWalletId() != null) {
                throw new IllegalArgumentException("Carteira de destino só pode ser informada em transferências.");
            }
            return null;
        }

        if (request.getDestinationWalletId() == null) {
            throw new IllegalArgumentException("Carteira de destino é obrigatória para transferências.");
        }

        ensureDifferentWallets(originWallet.getId(), request.getDestinationWalletId());

        Wallet destinationWallet = findWallet(
                request.getDestinationWalletId(),
                "Carteira de destino não encontrada com ID: "
        );
        walletAccessService.requireTransactionEdit(destinationWallet, currentUserId);
        return destinationWallet;
    }

    private Wallet resolveDestinationWalletForUpdate(
            Transaction transaction,
            UpdateTransactionRequest request,
            TransactionType newType,
            UUID currentUserId
    ) {
        if (newType != TransactionType.TRANSFER) {
            if (request.getDestinationWalletId() != null) {
                throw new IllegalArgumentException("Carteira de destino só pode ser informada em transferências.");
            }
            return null;
        }

        Wallet destinationWallet = transaction.getDestinationWallet();

        if (request.getDestinationWalletId() != null) {
            ensureDifferentWallets(transaction.getWallet().getId(), request.getDestinationWalletId());
            destinationWallet = findWallet(
                    request.getDestinationWalletId(),
                    "Carteira de destino não encontrada com ID: "
            );
        }

        if (destinationWallet == null) {
            throw new IllegalArgumentException("Transferências exigem uma carteira de destino.");
        }

        walletAccessService.requireTransactionEdit(destinationWallet, currentUserId);
        return destinationWallet;
    }

    private void ensureDifferentWallets(UUID originWalletId, UUID destinationWalletId) {
        if (originWalletId.equals(destinationWalletId)) {
            throw new IllegalArgumentException("A carteira de origem e destino não podem ser as mesmas.");
        }
    }

    private Category findCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada com ID: " + categoryId));
    }

    private void validateCategoryForWallet(Category category, Wallet wallet) {
        if (category == null) {
            throw new IllegalArgumentException("Categoria é obrigatória.");
        }

        if (Boolean.TRUE.equals(category.getSystemCategory()) && category.getWallet() == null) {
            return;
        }

        if (category.getWallet() == null
                || category.getWallet().getId() == null
                || !category.getWallet().getId().equals(wallet.getId())) {
            throw new AccessDeniedException("A categoria informada não pertence à carteira da transação.");
        }
    }

    private void validateCategoryType(Category category, TransactionType transactionType) {
        CategoryType expectedType = switch (transactionType) {
            case INCOME -> CategoryType.INCOME;
            case EXPENSE -> CategoryType.EXPENSE;
            case TRANSFER -> CategoryType.TRANSFER;
        };

        if (category.getType() != expectedType) {
            throw new IllegalArgumentException(switch (transactionType) {
                case INCOME -> "Receitas devem usar categorias do tipo INCOME.";
                case EXPENSE -> "Despesas devem usar categorias do tipo EXPENSE.";
                case TRANSFER -> "Transferências devem usar categorias do tipo TRANSFER.";
            });
        }
    }

    private void applyFinancialImpact(Transaction transaction, User user) {
        if (transaction.getType() == TransactionType.INCOME) {
            financialService.applyIncome(transaction.getWallet(), transaction, user);
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            financialService.applyExpense(transaction.getWallet(), transaction, user);
        } else if (transaction.getType() == TransactionType.TRANSFER) {
            financialService.applyTransfer(
                    transaction.getWallet(),
                    transaction.getDestinationWallet(),
                    transaction,
                    user
            );
        }
    }

    private Transaction snapshotForFinancialReversal(Transaction source) {
        Transaction snapshot = new Transaction();
        snapshot.setId(source.getId());
        snapshot.setWallet(source.getWallet());
        snapshot.setDestinationWallet(source.getDestinationWallet());
        snapshot.setCategory(source.getCategory());
        snapshot.setType(source.getType());
        snapshot.setAmount(source.getAmount());
        return snapshot;
    }

    private Wallet findWallet(UUID walletId, String messagePrefix) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException(messagePrefix + walletId));
    }

    private Transaction findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada com o ID: " + id));
    }
}
