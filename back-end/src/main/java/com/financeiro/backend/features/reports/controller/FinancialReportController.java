package com.financeiro.backend.features.reports.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Endpoints de relatórios, dashboards e indicadores consolidados.")
@SecurityRequirement(name = "bearerAuth")
public class FinancialReportController {

    @Autowired
    private FinancialReportService service;

    @GetMapping("/dashboard")
    @Operation(summary = "Resumo Consolidado do Dashboard")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getDashboard(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getDashboardSummary(filter, currentUserId)));
    }

    @GetMapping("/monthly")
    @Operation(summary = "Evolução Mensal (Receitas vs Despesas)")
    public ResponseEntity<ApiResponse<List<MonthlyBalanceResponse>>> getMonthlyBalance(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getMonthlyBalance(filter, currentUserId)));
    }

    @GetMapping("/categories")
    @Operation(summary = "Gastos Consolidados por Categoria")
    public ResponseEntity<ApiResponse<List<CategoryExpenseResponse>>> getExpensesByCategory(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getExpensesByCategory(filter, currentUserId)));
    }

    @GetMapping("/balance/history")
    @Operation(summary = "Evolução do Saldo Acumulado")
    public ResponseEntity<ApiResponse<List<BalanceHistoryResponse>>> getBalanceHistory(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getBalanceHistory(filter, currentUserId)));
    }

    @GetMapping("/cashflow")
    @Operation(summary = "Fluxo de Caixa Consolidado")
    public ResponseEntity<ApiResponse<List<CashFlowResponse>>> getCashFlow(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getCashFlow(filter, currentUserId)));
    }

    @GetMapping("/statement")
    @Operation(summary = "Extrato Financeiro com Múltiplos Filtros e Paginação")
    public ResponseEntity<ApiResponse<Page<StatementResponse>>> getStatement(
            @ModelAttribute ReportFilter filter,
            Pageable pageable,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getStatement(filter, pageable, currentUserId)));
    }

    @GetMapping("/indicators")
    @Operation(summary = "Indicadores (Métricas-chave)")
    public ResponseEntity<ApiResponse<IndicatorsResponse>> getIndicators(
            @ModelAttribute ReportFilter filter,
            @RequestAttribute("userId") UUID currentUserId) {
        return ResponseEntity.ok(ApiResponse.success(service.getIndicators(filter, currentUserId)));
    }
}
