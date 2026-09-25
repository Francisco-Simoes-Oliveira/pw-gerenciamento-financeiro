package com.financeiro.backend.features.reports;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import com.financeiro.backend.features.reports.repository.StatementRepository;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

class StatementReportLimitTest {
    @Test void oversizedReportsFailInsteadOfSilentlyTruncatingTotalsOrDownloads() {
        var rows = mock(StatementRepository.class);
        var wallets = mock(WalletRepository.class);
        var access = mock(WalletAccessService.class);
        UUID walletId = UUID.randomUUID(), userId = UUID.randomUUID();
        var wallet = Wallet.builder().id(walletId).build();
        when(wallets.findById(walletId)).thenReturn(Optional.of(wallet));
        when(rows.findForStatement(eq(walletId), any(), any(), any())).thenReturn(Collections.nCopies(10_001, new Transaction()));
        assertThrows(IllegalArgumentException.class, () -> new StatementReportService(rows, wallets, access)
                .generate(walletId, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), userId));
        verify(access).requireView(wallet, userId);
    }
}
