package com.financeiro.backend.features.reports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.reports.repository.StatementRepository;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@Service
public class StatementReportService {
    private static final int MAX_ENTRIES = 10_000;
    private final StatementRepository transactions;
    private final WalletRepository wallets;
    private final WalletAccessService access;

    public StatementReportService(StatementRepository transactions, WalletRepository wallets, WalletAccessService access) {
        this.transactions = transactions;
        this.wallets = wallets;
        this.access = access;
    }

    @Transactional(readOnly = true)
    public StatementReport generate(UUID walletId, LocalDate start, LocalDate end, UUID userId) {
        if (walletId == null) throw new IllegalArgumentException("Selecione uma carteira.");
        if (start == null && end == null) {
            start = LocalDate.now().withDayOfMonth(1);
            end = start.plusMonths(1).minusDays(1);
        }
        if (start == null || end == null || end.isBefore(start)
                || start.getYear() < 1900 || end.getYear() > 9998
                || ChronoUnit.DAYS.between(start, end) >= 366) {
            throw new IllegalArgumentException("Informe um período válido de até 366 dias, com início e fim.");
        }
        var wallet = wallets.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        // Always authorize before reading transactions; VIEWER and EDITOR can export too.
        access.requireView(wallet, userId);
        var rows = transactions.findForStatement(walletId, start.atStartOfDay(), end.plusDays(1).atStartOfDay(),
                PageRequest.of(0, MAX_ENTRIES + 1));
        if (rows.size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("O extrato excede 10.000 lançamentos. Reduza o período.");
        }
        BigDecimal income = BigDecimal.ZERO, expense = BigDecimal.ZERO;
        BigDecimal incoming = BigDecimal.ZERO, outgoing = BigDecimal.ZERO;
        long pending = 0, canceled = 0;
        var entries = new ArrayList<StatementReport.Entry>();
        for (var tx : rows) {
            boolean origin = walletId.equals(tx.getWallet().getId());
            String movement = tx.getType() == TransactionType.TRANSFER
                    ? (origin ? "TRANSFER_OUT" : "TRANSFER_IN") : tx.getType().name();
            BigDecimal amount = tx.getAmount();
            var status = tx.getStatus() == null ? TransactionStatus.PENDING : tx.getStatus();
            BigDecimal impact = BigDecimal.ZERO;
            if (status == TransactionStatus.PAID) {
                switch (movement) {
                    case "INCOME" -> { income = income.add(amount); impact = amount; }
                    case "EXPENSE" -> { expense = expense.add(amount); impact = amount.negate(); }
                    case "TRANSFER_IN" -> { incoming = incoming.add(amount); impact = amount; }
                    case "TRANSFER_OUT" -> { outgoing = outgoing.add(amount); impact = amount.negate(); }
                    default -> throw new IllegalStateException("Tipo de lançamento inválido.");
                }
            } else if (status == TransactionStatus.PENDING) pending++;
            else canceled++;
            // Incoming transfers never expose categories, names or IDs from the other wallet.
            entries.add(new StatementReport.Entry(tx.getId(),
                    tx.getTransactionDate() != null ? tx.getTransactionDate() : tx.getCreatedAt(),
                    origin ? tx.getTitle() : "Transferência recebida",
                    origin && tx.getCategory() != null ? tx.getCategory().getName() : null,
                    movement, status, amount, impact));
        }
        return new StatementReport(walletId, wallet.getName(), wallet.getCurrency() == null ? "BRL" : wallet.getCurrency(),
                start, end, LocalDateTime.now(), income, expense, incoming, outgoing,
                income.subtract(expense).add(incoming).subtract(outgoing), pending, canceled, List.copyOf(entries));
    }
}
