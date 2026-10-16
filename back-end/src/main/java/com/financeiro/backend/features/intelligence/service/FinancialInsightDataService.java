package com.financeiro.backend.features.intelligence.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.financeiro.backend.features.intelligence.model.FinancialInsightData;
import com.financeiro.backend.features.intelligence.model.InsightJob;
import com.financeiro.backend.features.reports.dto.response.StatementReport;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;

@Service
public class FinancialInsightDataService {
    private final StatementReportService reports;

    public FinancialInsightDataService(StatementReportService reports) {
        this.reports = reports;
    }

    public FinancialInsightData build(InsightJob job) {
        long days = ChronoUnit.DAYS.between(job.startDate(), job.endDate()) + 1;
        var previousEnd = job.startDate().minusDays(1);
        var previousStart = previousEnd.minusDays(days - 1);
        // StatementReportService repeats wallet authorization here, protecting against
        // membership revocation between ticket creation and SSE connection.
        var current = reports.generate(job.walletId(), job.startDate(), job.endDate(), job.userId());
        var previous = reports.generate(job.walletId(), previousStart, previousEnd, job.userId());

        Map<String, BigDecimal> currentCategories = expenseCategories(current);
        Map<String, BigDecimal> previousCategories = expenseCategories(previous);
        var names = new java.util.TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        names.addAll(currentCategories.keySet());
        names.addAll(previousCategories.keySet());
        var categories = names.stream()
                .map(name -> new FinancialInsightData.CategoryComparison(name,
                        currentCategories.getOrDefault(name, BigDecimal.ZERO),
                        previousCategories.getOrDefault(name, BigDecimal.ZERO)))
                .sorted(java.util.Comparator.comparing(FinancialInsightData.CategoryComparison::currentTotal).reversed())
                .limit(20)
                .toList();

        BigDecimal change = previous.expense().compareTo(BigDecimal.ZERO) == 0 ? null
                : current.expense().subtract(previous.expense())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(previous.expense(), 2, RoundingMode.HALF_UP);
        long paidCount = current.entries().stream().filter(entry -> entry.status() == TransactionStatus.PAID).count();
        return new FinancialInsightData(job.startDate().toString(), job.endDate().toString(), current.currency(),
                totals(current), totals(previous), change, paidCount, categories);
    }

    private Map<String, BigDecimal> expenseCategories(StatementReport report) {
        Map<String, BigDecimal> totals = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        report.entries().stream()
                .filter(entry -> entry.status() == TransactionStatus.PAID && "EXPENSE".equals(entry.movement()))
                .forEach(entry -> totals.merge(sanitizeCategory(entry.category()), entry.amount(), BigDecimal::add));
        return totals;
    }

    static String sanitizeCategory(String value) {
        if (value == null || value.isBlank()) return "Sem categoria";
        String clean = value.replaceAll("[\\p{Cntrl}]", " ").replaceAll("\\s+", " ").trim();
        return clean.length() <= 80 ? clean : clean.substring(0, 80);
    }

    private FinancialInsightData.PeriodTotals totals(StatementReport report) {
        return new FinancialInsightData.PeriodTotals(report.income(), report.expense(), report.transferIn(),
                report.transferOut(), report.netChange());
    }
}
