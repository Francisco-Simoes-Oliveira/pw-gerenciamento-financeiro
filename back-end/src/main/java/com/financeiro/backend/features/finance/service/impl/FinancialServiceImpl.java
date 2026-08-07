package com.financeiro.backend.features.finance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.finance.entity.FinancialAudit;
import com.financeiro.backend.features.finance.repository.FinancialAuditRepository;
import com.financeiro.backend.features.finance.service.FinancialService;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.transaction.repository.TransactionRepository;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;

@Service
public class FinancialServiceImpl implements FinancialService {

    private static final Logger log = LoggerFactory.getLogger(FinancialServiceImpl.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private FinancialAuditRepository auditRepository;
    
    @Autowired
    private TransactionRepository transactionRepository;

    private BigDecimal formatAmount(BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    @Transactional
    public void applyIncome(Wallet wallet, Transaction transaction, User user) {
        BigDecimal amount = formatAmount(transaction.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da receita deve ser maior que zero.");
        }
        
        BigDecimal oldBalance = formatAmount(wallet.getBalance());
        wallet.setBalance(oldBalance.add(amount));
        wallet.setLastBalanceUpdate(LocalDateTime.now());
        
        walletRepository.save(wallet);
        
        registerAudit(wallet, transaction, user, "APPLY_INCOME", oldBalance, wallet.getBalance(), amount);
    }

    @Override
    @Transactional
    public void applyExpense(Wallet wallet, Transaction transaction, User user) {
        BigDecimal amount = formatAmount(transaction.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da despesa deve ser maior que zero.");
        }
        
        BigDecimal oldBalance = formatAmount(wallet.getBalance());
        wallet.setBalance(oldBalance.subtract(amount));
        wallet.setLastBalanceUpdate(LocalDateTime.now());
        
        walletRepository.save(wallet);
        
        registerAudit(wallet, transaction, user, "APPLY_EXPENSE", oldBalance, wallet.getBalance(), amount);
    }

    @Override
    @Transactional
    public void applyTransfer(Wallet origin, Wallet destination, Transaction transaction, User user) {
        BigDecimal amount = formatAmount(transaction.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero.");
        }
        
        // Debita da origem
        BigDecimal oldOriginBalance = formatAmount(origin.getBalance());
        origin.setBalance(oldOriginBalance.subtract(amount));
        origin.setLastBalanceUpdate(LocalDateTime.now());
        walletRepository.save(origin);
        registerAudit(origin, transaction, user, "TRANSFER_OUT", oldOriginBalance, origin.getBalance(), amount);
        
        // Credita no destino
        BigDecimal oldDestBalance = formatAmount(destination.getBalance());
        destination.setBalance(oldDestBalance.add(amount));
        destination.setLastBalanceUpdate(LocalDateTime.now());
        walletRepository.save(destination);
        registerAudit(destination, transaction, user, "TRANSFER_IN", oldDestBalance, destination.getBalance(), amount);
    }

    @Override
    @Transactional
    public void updateTransaction(Transaction oldTransaction, Transaction newTransaction, User user) {
        // Primeiro reverte a transação antiga
        revertTransaction(oldTransaction, user);
        
        // Depois aplica a nova
        if (newTransaction.getType() == TransactionType.INCOME) {
            applyIncome(newTransaction.getWallet(), newTransaction, user);
        } else if (newTransaction.getType() == TransactionType.EXPENSE) {
            applyExpense(newTransaction.getWallet(), newTransaction, user);
        } else if (newTransaction.getType() == TransactionType.TRANSFER) {
            applyTransfer(newTransaction.getWallet(), newTransaction.getDestinationWallet(), newTransaction, user);
        }
    }

    @Override
    @Transactional
    public void deleteTransaction(Transaction transaction, User user) {
        revertTransaction(transaction, user);
    }

    @Override
    @Transactional
    public void revertTransaction(Transaction transaction, User user) {
        BigDecimal amount = formatAmount(transaction.getAmount());
        Wallet wallet = transaction.getWallet();
        BigDecimal oldBalance = formatAmount(wallet.getBalance());
        
        if (transaction.getType() == TransactionType.INCOME) {
            wallet.setBalance(oldBalance.subtract(amount));
            wallet.setLastBalanceUpdate(LocalDateTime.now());
            walletRepository.save(wallet);
            registerAudit(wallet, transaction, user, "REVERT_INCOME", oldBalance, wallet.getBalance(), amount);
            
        } else if (transaction.getType() == TransactionType.EXPENSE) {
            wallet.setBalance(oldBalance.add(amount));
            wallet.setLastBalanceUpdate(LocalDateTime.now());
            walletRepository.save(wallet);
            registerAudit(wallet, transaction, user, "REVERT_EXPENSE", oldBalance, wallet.getBalance(), amount);
            
        } else if (transaction.getType() == TransactionType.TRANSFER) {
            // Reverte origem
            wallet.setBalance(oldBalance.add(amount));
            wallet.setLastBalanceUpdate(LocalDateTime.now());
            walletRepository.save(wallet);
            registerAudit(wallet, transaction, user, "REVERT_TRANSFER_OUT", oldBalance, wallet.getBalance(), amount);
            
            // Reverte destino
            Wallet dest = transaction.getDestinationWallet();
            if (dest != null) {
                BigDecimal oldDestBalance = formatAmount(dest.getBalance());
                dest.setBalance(oldDestBalance.subtract(amount));
                dest.setLastBalanceUpdate(LocalDateTime.now());
                walletRepository.save(dest);
                registerAudit(dest, transaction, user, "REVERT_TRANSFER_IN", oldDestBalance, dest.getBalance(), amount);
            }
        }
    }

    @Override
    @Transactional
    public void repairWalletBalance(UUID walletId) {
        recalculateWalletBalance(walletId);
    }
    
    private void recalculateWalletBalance(UUID walletId) {
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada para recálculo."));
                
        // Encontrar transações onde esta carteira é origem
        List<Transaction> originTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getWallet() != null && t.getWallet().getId().equals(walletId))
                .toList();
                
        // Encontrar transações onde esta carteira é destino (TRANSFER)
        List<Transaction> destTransactions = transactionRepository.findAll().stream()
                .filter(t -> t.getDestinationWallet() != null && t.getDestinationWallet().getId().equals(walletId))
                .toList();
                
        BigDecimal calculatedBalance = BigDecimal.ZERO;
        
        for (Transaction t : originTransactions) {
            BigDecimal amt = formatAmount(t.getAmount());
            if (t.getType() == TransactionType.INCOME) {
                calculatedBalance = calculatedBalance.add(amt);
            } else if (t.getType() == TransactionType.EXPENSE || t.getType() == TransactionType.TRANSFER) {
                calculatedBalance = calculatedBalance.subtract(amt);
            }
        }
        
        for (Transaction t : destTransactions) {
            BigDecimal amt = formatAmount(t.getAmount());
            if (t.getType() == TransactionType.TRANSFER) {
                calculatedBalance = calculatedBalance.add(amt);
            }
        }
        
        BigDecimal oldBalance = formatAmount(wallet.getBalance());
        wallet.setBalance(calculatedBalance);
        wallet.setLastBalanceUpdate(LocalDateTime.now());
        walletRepository.save(wallet);
        
        registerAudit(wallet, null, wallet.getOwner(), "RECALCULATE", oldBalance, calculatedBalance, BigDecimal.ZERO);
    }
    
    private void registerAudit(Wallet wallet, Transaction transaction, User user, String operation, BigDecimal oldBalance, BigDecimal newBalance, BigDecimal amount) {
        FinancialAudit audit = FinancialAudit.builder()
                .wallet(wallet)
                .transaction(transaction)
                .user(user)
                .operation(operation)
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
                
        auditRepository.save(audit);
        
        log.info("Carteira [{}]: Saldo anterior [{}]. Operacao [{}]. Valor [{}]. Saldo final [{}]. Usuario [{}] Data [{}]",
                wallet.getId(), oldBalance, operation, amount, newBalance, user.getId(), audit.getCreatedAt());
    }
}
