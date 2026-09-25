package com.financeiro.backend.features.reports.controller;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.features.reports.service.StatementExportService;
import com.financeiro.backend.security.services.UserDetailsImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/reports/statement")
@SecurityRequirement(name = "bearerAuth")
public class StatementReportController {
    private final StatementReportService reports;
    private final StatementExportService exports;

    public StatementReportController(StatementReportService reports, StatementExportService exports) {
        this.reports = reports;
        this.exports = exports;
    }

    @GetMapping("/report")
    @Operation(summary = "Extrato por carteira e período, com totais realizados")
    public ResponseEntity<ApiResponse<StatementReport>> report(
            @RequestParam UUID walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetailsImpl user) {
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(ApiResponse.success(reports.generate(walletId, startDate, endDate, user.getId())));
    }

    @GetMapping("/export")
    @Operation(summary = "Exportar extrato completo em PDF ou CSV (até 10.000 lançamentos)")
    public ResponseEntity<byte[]> export(
            @RequestParam UUID walletId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String format,
            @AuthenticationPrincipal UserDetailsImpl user) {
        String normalized = format.toLowerCase(Locale.ROOT);
        if (!normalized.equals("csv") && !normalized.equals("pdf")) {
            throw new IllegalArgumentException("Formato permitido: pdf ou csv.");
        }
        var report = reports.generate(walletId, startDate, endDate, user.getId());
        byte[] bytes = normalized.equals("csv") ? exports.csv(report) : exports.pdf(report);
        return ResponseEntity.ok()
                .contentType(normalized.equals("csv") ? MediaType.parseMediaType("text/csv;charset=UTF-8") : MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"extrato-" + report.startDate()
                        + "-" + report.endDate() + "." + normalized + "\"")
                .body(bytes);
    }
}
