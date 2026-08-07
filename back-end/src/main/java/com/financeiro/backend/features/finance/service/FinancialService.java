package com.financeiro.backend.features.finance.service;

import java.math.BigDecimal;
import java.util.UUID;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.wallet.entity.Wallet;

public interface FinancialService {
    void applyIncome(Wallet wallet, Transaction transaction, User user);
    void applyExpense(Wallet wallet, Transaction transaction, User user);
    void applyTransfer(Wallet origin, Wallet destination, Transaction transaction, User user);
    
    void updateTransaction(Transaction oldTransaction, Transaction newTransaction, User user);
    void deleteTransaction(Transaction transaction, User user);
    void revertTransaction(Transaction transaction, User user);
    
    void repairWalletBalance(UUID walletId);
}
