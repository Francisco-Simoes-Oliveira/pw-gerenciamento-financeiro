package com.financeiro.backend.features.reports.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.reports.dto.response.BalanceHistoryResponse;
import com.financeiro.backend.features.reports.dto.response.CashFlowResponse;
import com.financeiro.backend.features.reports.dto.response.CategoryExpenseResponse;
import com.financeiro.backend.features.reports.dto.response.DashboardSummaryResponse;
import com.financeiro.backend.features.reports.dto.response.IndicatorsResponse;
import com.financeiro.backend.features.reports.dto.response.MonthlyBalanceResponse;
import com.financeiro.backend.features.reports.dto.response.StatementResponse;
import com.financeiro.backend.features.reports.service.FinancialReportService;
import com.financeiro.backend.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints de relatórios, dashboards e indicadores consolidados.")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "http://localhost:5174", maxAge = 3600)
public class FinancialReportController {

    @Autowired
    private FinancialReportService service;

    private UUID getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getId();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Resumo Consolidado do Dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getDashboardSummary(filter, getCurrentUserId())));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Evolução Mensal (Receitas vs Despesas)")
    public ResponseEntity<ApiResponse<List<MonthlyBalanceResponse>>> getMonthlyBalance(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getMonthlyBalance(filter, getCurrentUserId())));
    }

    @GetMapping("/categories")
    @Operation(summary = "Gastos Consolidados por Categoria")
    public ResponseEntity<ApiResponse<List<CategoryExpenseResponse>>> getExpensesByCategory(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getExpensesByCategory(filter, getCurrentUserId())));
    }

    @GetMapping("/balance/history")
    @Operation(summary = "Evolução do Saldo Acumulado")
    public ResponseEntity<ApiResponse<List<BalanceHistoryResponse>>> getBalanceHistory(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getBalanceHistory(filter, getCurrentUserId())));
    }

    @GetMapping("/cashflow")
    @Operation(summary = "Fluxo de Caixa Consolidado")
    public ResponseEntity<ApiResponse<List<CashFlowResponse>>> getCashFlow(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getCashFlow(filter, getCurrentUserId())));
    }

    @GetMapping("/statement")
    @Operation(summary = "Extrato Financeiro com Múltiplos Filtros e Paginação")
    public ResponseEntity<ApiResponse<Page<StatementResponse>>> getStatement(
            @ModelAttribute ReportFilter filter,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(service.getStatement(filter, pageable, getCurrentUserId())));
    }

    @GetMapping("/indicators")
    @Operation(summary = "Indicadores (Métricas-chave)")
    public ResponseEntity<ApiResponse<IndicatorsResponse>> getIndicators(
            @ModelAttribute ReportFilter filter) {
        return ResponseEntity.ok(ApiResponse.success(service.getIndicators(filter, getCurrentUserId())));
    }
}
