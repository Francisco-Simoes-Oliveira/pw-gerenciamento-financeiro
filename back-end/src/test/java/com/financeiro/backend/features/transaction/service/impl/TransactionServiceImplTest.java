package com.financeiro.backend.features.transaction.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.category.enums.CategoryType;
import com.financeiro.backend.features.category.repository.CategoryRepository;
import com.financeiro.backend.features.finance.service.FinancialService;
import com.financeiro.backend.features.realtime.event.TransactionChangedEvent;
import com.financeiro.backend.features.transaction.dto.request.CreateTransactionRequest;
import com.financeiro.backend.features.transaction.dto.response.TransactionResponse;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.transaction.mapper.TransactionMapper;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper mapper;

    @Mock
    private FinancialService financialService;

    @Mock
    private WalletAccessService walletAccessService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User owner;
    private Wallet wallet;
    private Category categoryIncome;
    private Category categoryExpense;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(UUID.randomUUID());

        wallet = Wallet.builder().id(UUID.randomUUID()).owner(owner).build();

        categoryIncome = Category.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(CategoryType.INCOME)
                .systemCategory(false)
                .build();
        categoryExpense = Category.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(CategoryType.EXPENSE)
                .systemCategory(false)
                .build();
    }

    @Test
    void insertWithInvalidCategoryTypeShouldThrow() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(wallet.getId());
        request.setCategoryId(categoryExpense.getId());
        request.setType(TransactionType.INCOME);
        request.setAmount(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(categoryExpense.getId())).thenReturn(Optional.of(categoryExpense));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.insert(owner.getId(), request));

        assertEquals("Receitas devem usar categorias do tipo INCOME.", ex.getMessage());
    }

    @Test
    void insertTransferToSameWalletShouldThrow() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(wallet.getId());
        request.setDestinationWalletId(wallet.getId());
        request.setType(TransactionType.TRANSFER);

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.insert(owner.getId(), request));

        assertEquals("A carteira de origem e destino não podem ser as mesmas.", ex.getMessage());
    }

    @Test
    void insertWithCategoryFromAnotherWalletShouldBeDenied() {
        User otherOwner = new User();
        otherOwner.setId(UUID.randomUUID());
        Wallet otherWallet = Wallet.builder().id(UUID.randomUUID()).owner(otherOwner).build();
        Category foreignCategory = Category.builder()
                .id(UUID.randomUUID())
                .wallet(otherWallet)
                .type(CategoryType.INCOME)
                .systemCategory(false)
                .build();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(wallet.getId());
        request.setCategoryId(foreignCategory.getId());
        request.setType(TransactionType.INCOME);
        request.setAmount(BigDecimal.TEN);

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(foreignCategory.getId())).thenReturn(Optional.of(foreignCategory));

        assertThrows(AccessDeniedException.class,
                () -> transactionService.insert(owner.getId(), request));

        verify(repository, never()).save(any());
        verify(financialService, never()).applyIncome(any(), any(), any());
    }

    @Test
    void insertSuccessShouldApplyFinancialImpact() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(wallet.getId());
        request.setCategoryId(categoryIncome.getId());
        request.setType(TransactionType.INCOME);
        request.setAmount(BigDecimal.valueOf(100.00));

        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        when(categoryRepository.findById(categoryIncome.getId())).thenReturn(Optional.of(categoryIncome));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        Transaction tx = Transaction.builder()
                .type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(100.00))
                .build();
        when(mapper.toEntity(request)).thenReturn(tx);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.toResponse(any())).thenReturn(new TransactionResponse());

        transactionService.insert(owner.getId(), request);

        verify(walletAccessService).requireTransactionEdit(wallet, owner.getId());
        verify(financialService, times(1)).applyIncome(wallet, tx, owner);
        verify(eventPublisher, times(1)).publishEvent((Object) any(TransactionChangedEvent.class));
    }

    @Test
    void deleteSuccessShouldReverseFinancialImpact() {
        Transaction tx = Transaction.builder()
                .id(UUID.randomUUID())
                .wallet(wallet)
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(50))
                .build();

        when(repository.findById(tx.getId())).thenReturn(Optional.of(tx));
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));

        transactionService.remove(tx.getId(), owner.getId());

        verify(walletAccessService).requireTransactionEdit(wallet, owner.getId());
        verify(financialService, times(1)).deleteTransaction(tx, owner);
        verify(repository, times(1)).delete(tx);
        verify(eventPublisher, times(1)).publishEvent((Object) any(TransactionChangedEvent.class));
    }
}
