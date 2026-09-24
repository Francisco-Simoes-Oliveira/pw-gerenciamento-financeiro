package com.financeiro.backend.features.reports.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.reports.dto.response.BalanceHistoryResponse;
import com.financeiro.backend.features.reports.dto.response.CashFlowResponse;
import com.financeiro.backend.features.reports.dto.response.CategoryExpenseResponse;
import com.financeiro.backend.features.reports.dto.response.DashboardSummaryResponse;
import com.financeiro.backend.features.reports.dto.response.IndicatorsResponse;
import com.financeiro.backend.features.reports.dto.response.MonthlyBalanceResponse;
import com.financeiro.backend.features.reports.dto.response.StatementResponse;

public interface FinancialReportService {
    
    DashboardSummaryResponse getDashboardSummary(ReportFilter filter, UUID currentUserId);
    
    List<MonthlyBalanceResponse> getMonthlyBalance(ReportFilter filter, UUID currentUserId);
    
    List<CategoryExpenseResponse> getExpensesByCategory(ReportFilter filter, UUID currentUserId);
    
    List<BalanceHistoryResponse> getBalanceHistory(ReportFilter filter, UUID currentUserId);
    
    List<CashFlowResponse> getCashFlow(ReportFilter filter, UUID currentUserId);
    
    Page<StatementResponse> getStatement(ReportFilter filter, Pageable pageable, UUID currentUserId);
    
    IndicatorsResponse getIndicators(ReportFilter filter, UUID currentUserId);
}
