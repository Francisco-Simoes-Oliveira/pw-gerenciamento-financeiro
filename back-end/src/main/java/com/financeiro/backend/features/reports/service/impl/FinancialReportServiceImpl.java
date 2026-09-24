package com.financeiro.backend.features.reports.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.reports.dto.response.BalanceHistoryResponse;
import com.financeiro.backend.features.reports.dto.response.CashFlowResponse;
import com.financeiro.backend.features.reports.dto.response.CategoryExpenseResponse;
import com.financeiro.backend.features.reports.dto.response.DashboardSummaryResponse;
import com.financeiro.backend.features.reports.dto.response.IndicatorsResponse;
import com.financeiro.backend.features.reports.dto.response.MonthlyBalanceResponse;
import com.financeiro.backend.features.reports.dto.response.StatementResponse;
import com.financeiro.backend.features.reports.projection.DashboardProjection;
import com.financeiro.backend.features.reports.projection.IndicatorProjection;
import com.financeiro.backend.features.reports.projection.MonthlyProjection;
import com.financeiro.backend.features.reports.repository.CategoryReportRepository;
import com.financeiro.backend.features.reports.repository.DashboardRepository;
import com.financeiro.backend.features.reports.repository.IndicatorRepository;
import com.financeiro.backend.features.reports.repository.StatementRepository;
import com.financeiro.backend.features.reports.service.FinancialReportService;
import com.financeiro.backend.features.reports.specification.TransactionSpecification;
import com.financeiro.backend.features.transaction.entity.Transaction;

@Service
public class FinancialReportServiceImpl implements FinancialReportService {

    @Autowired
    private DashboardRepository dashboardRepository;
    
    @Autowired
    private StatementRepository statementRepository;
    
    @Autowired
    private IndicatorRepository indicatorRepository;
    
    @Autowired
    private CategoryReportRepository categoryReportRepository;

    private BigDecimal zeroIfNull(BigDecimal val) {
        return val == null ? BigDecimal.ZERO : val.setScale(2, RoundingMode.HALF_EVEN);
    }

    @Override
    @Cacheable(value = "dashboard", key = "#currentUserId + '_' + (#filter.walletId != null ? #filter.walletId : 'ALL')")
    public DashboardSummaryResponse getDashboardSummary(ReportFilter filter, UUID currentUserId) {
        DashboardProjection proj = dashboardRepository.getDashboardConsolidated(currentUserId, filter.getWalletId());
        
        if (proj == null) {
            return new DashboardSummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L, 0L);
        }

        BigDecimal income = zeroIfNull(proj.getIncome());
        BigDecimal expense = zeroIfNull(proj.getExpense());
        BigDecimal transfer = zeroIfNull(proj.getTransfer());
        BigDecimal balance = income.subtract(expense); // Basic calc, assuming past data is static

        return DashboardSummaryResponse.builder()
                .currentBalance(balance)
                .monthlyIncome(income)
                .monthlyExpense(expense)
                .monthlyTransfer(transfer)
                .transactions(proj.getTransactionsCount() == null ? 0 : proj.getTransactionsCount())
                // wallets and categories will need separate queries if we want absolute totals, or we get it from Indicator projection
                .wallets(0L)
                .categories(0L)
                .build();
    }

    @Override
    @Cacheable(value = "monthly", key = "#currentUserId + '_' + (#filter.walletId != null ? #filter.walletId : 'ALL')")
    public List<MonthlyBalanceResponse> getMonthlyBalance(ReportFilter filter, UUID currentUserId) {
        List<MonthlyProjection> projs = dashboardRepository.getMonthlyBalance(currentUserId, filter.getWalletId());
        
        return projs.stream().map(p -> {
            BigDecimal inc = zeroIfNull(p.getIncome());
            BigDecimal exp = zeroIfNull(p.getExpense());
            return MonthlyBalanceResponse.builder()
                    .month(String.format("%04d-%02d", p.getYear(), p.getMonth()))
                    .income(inc)
                    .expense(exp)
                    .balance(inc.subtract(exp))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories", key = "#currentUserId + '_' + (#filter.walletId != null ? #filter.walletId : 'ALL')")
    public List<CategoryExpenseResponse> getExpensesByCategory(ReportFilter filter, UUID currentUserId) {
        var projs = categoryReportRepository.getExpensesByCategory(currentUserId, filter.getWalletId());
        
        BigDecimal totalExpenses = projs.stream()
                .map(p -> zeroIfNull(p.getTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        return projs.stream().map(p -> {
            BigDecimal total = zeroIfNull(p.getTotal());
            Double pct = totalExpenses.compareTo(BigDecimal.ZERO) == 0 ? 0.0 
                       : total.multiply(new BigDecimal(100)).divide(totalExpenses, 2, RoundingMode.HALF_EVEN).doubleValue();
            return CategoryExpenseResponse.builder()
                    .category(p.getCategoryName())
                    .total(total)
                    .percentage(pct)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public List<BalanceHistoryResponse> getBalanceHistory(ReportFilter filter, UUID currentUserId) {
        // Will be implemented later with CashFlow projections, returning empty list for now
        return List.of();
    }

    @Override
    public List<CashFlowResponse> getCashFlow(ReportFilter filter, UUID currentUserId) {
        // Will be implemented later with CashFlow projections, returning empty list for now
        return List.of();
    }

    @Override
    public Page<StatementResponse> getStatement(ReportFilter filter, Pageable pageable, UUID currentUserId) {
        Page<Transaction> page = statementRepository.findAll(TransactionSpecification.withFilter(filter, currentUserId), pageable);
        
        return page.map(t -> StatementResponse.builder()
                .transactionId(t.getId())
                .walletName(t.getWallet() != null ? t.getWallet().getName() : null)
                .destinationWalletName(t.getDestinationWallet() != null ? t.getDestinationWallet().getName() : null)
                .categoryName(t.getCategory() != null ? t.getCategory().getName() : null)
                .categoryColor(t.getCategory() != null ? t.getCategory().getColor() : null)
                .categoryIcon(t.getCategory() != null ? t.getCategory().getIcon() : null)
                .createdByName(t.getCreatedBy() != null ? t.getCreatedBy().getName() : null)
                .type(t.getType())
                .status(t.getStatus())
                .title(t.getTitle())
                .description(t.getDescription())
                .attachmentUrl(t.getAttachmentUrl())
                .amount(t.getAmount())
                .transactionDate(t.getCreatedAt()) // Emulating balanceAfterOperation might be complex here
                .build());
    }

    @Override
    @Cacheable(value = "indicators", key = "#currentUserId + '_' + (#filter.walletId != null ? #filter.walletId : 'ALL')")
    public IndicatorsResponse getIndicators(ReportFilter filter, UUID currentUserId) {
        IndicatorProjection proj = indicatorRepository.getIndicators(currentUserId, filter.getWalletId());
        if (proj == null) {
            return new IndicatorsResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, BigDecimal.ZERO, null, null);
        }
        
        return IndicatorsResponse.builder()
                .maxIncome(zeroIfNull(proj.getMaxIncome()))
                .maxExpense(zeroIfNull(proj.getMaxExpense()))
                .transactionCount(proj.getTransactionCount() == null ? 0 : proj.getTransactionCount())
                .build();
    }
}
