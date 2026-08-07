package com.financeiro.backend.features.finance.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.finance.repository.FinancialAuditRepository;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class FinancialServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private FinancialAuditRepository auditRepository;
    
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private FinancialServiceImpl financialService;

    private User user;
    private Wallet wallet;
    private Wallet destWallet;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        
        wallet = Wallet.builder().id(UUID.randomUUID()).balance(BigDecimal.valueOf(100.00)).owner(user).build();
        destWallet = Wallet.builder().id(UUID.randomUUID()).balance(BigDecimal.valueOf(50.00)).owner(user).build();
        transaction = Transaction.builder().id(UUID.randomUUID()).amount(BigDecimal.valueOf(50.00)).wallet(wallet).type(TransactionType.INCOME).build();
    }

    @Test
    void testApplyIncome_Success() {
        financialService.applyIncome(wallet, transaction, user);

        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(wallet.getBalance()));
        verify(walletRepository, times(1)).save(wallet);
        verify(auditRepository, times(1)).save(any());
    }

    @Test
    void testApplyIncome_NegativeAmount() {
        transaction.setAmount(BigDecimal.valueOf(-10.0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            financialService.applyIncome(wallet, transaction, user);
        });
        assertEquals("Valor da receita deve ser maior que zero.", exception.getMessage());
    }

    @Test
    void testApplyExpense_Success() {
        transaction.setType(TransactionType.EXPENSE);
        financialService.applyExpense(wallet, transaction, user);

        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(wallet.getBalance()));
        verify(walletRepository, times(1)).save(wallet);
        verify(auditRepository, times(1)).save(any());
    }

    @Test
    void testApplyTransfer_Success() {
        transaction.setType(TransactionType.TRANSFER);
        financialService.applyTransfer(wallet, destWallet, transaction, user);

        // Origem (100 - 50 = 50)
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(wallet.getBalance()));
        // Destino (50 + 50 = 100)
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(destWallet.getBalance()));
        
        verify(walletRepository, times(1)).save(wallet);
        verify(walletRepository, times(1)).save(destWallet);
        verify(auditRepository, times(2)).save(any());
    }
    
    @Test
    void testRevertIncome_Success() {
        transaction.setType(TransactionType.INCOME);
        financialService.revertTransaction(transaction, user);
        
        // Se tinha 100, e reverti a renda de 50, fica 50.
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(wallet.getBalance()));
        verify(walletRepository, times(1)).save(wallet);
    }
    
    @Test
    void testRevertTransfer_Success() {
        transaction.setType(TransactionType.TRANSFER);
        transaction.setDestinationWallet(destWallet);
        
        financialService.revertTransaction(transaction, user);
        
        // Origem devoluçao (100 + 50 = 150)
        assertEquals(0, BigDecimal.valueOf(150.00).compareTo(wallet.getBalance()));
        // Destino devoluçao (50 - 50 = 0)
        assertEquals(0, BigDecimal.valueOf(0.00).compareTo(destWallet.getBalance()));
        
        verify(walletRepository, times(1)).save(wallet);
        verify(walletRepository, times(1)).save(destWallet);
    }

    @Test
    void testRecalculateWalletBalance_Success() {
        when(walletRepository.findById(wallet.getId())).thenReturn(Optional.of(wallet));
        
        Transaction t1 = Transaction.builder().wallet(wallet).type(TransactionType.INCOME).amount(BigDecimal.valueOf(200.00)).build();
        Transaction t2 = Transaction.builder().wallet(wallet).type(TransactionType.EXPENSE).amount(BigDecimal.valueOf(50.00)).build();
        Transaction t3 = Transaction.builder().destinationWallet(wallet).type(TransactionType.TRANSFER).amount(BigDecimal.valueOf(20.00)).build();
        
        when(transactionRepository.findAll()).thenReturn(List.of(t1, t2, t3));

        financialService.repairWalletBalance(wallet.getId());

        // Balance should be: 200 (INCOME) - 50 (EXPENSE) + 20 (TRANSFER IN) = 170
        assertEquals(0, BigDecimal.valueOf(170.00).compareTo(wallet.getBalance()));
        verify(walletRepository, times(1)).save(wallet);
    }
}
