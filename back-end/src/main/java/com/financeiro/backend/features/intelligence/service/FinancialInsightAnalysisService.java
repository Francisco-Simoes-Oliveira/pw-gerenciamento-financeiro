package com.financeiro.backend.features.intelligence.service;

import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.financeiro.backend.features.intelligence.dto.response.InsightProgress;
import com.financeiro.backend.features.intelligence.dto.response.InsightResult;
import com.financeiro.backend.features.intelligence.model.InsightJob;

@Service
public class FinancialInsightAnalysisService {
    private final FinancialInsightDataService dataService;
    private final FinancialInsightProvider provider;

    public FinancialInsightAnalysisService(FinancialInsightDataService dataService, FinancialInsightProvider provider) {
        this.dataService = dataService;
        this.provider = provider;
    }

    public InsightResult analyze(InsightJob job, Consumer<InsightProgress> progress) {
        progress.accept(new InsightProgress("PREPARING_DATA", "Preparando dados financeiros..."));
        var data = dataService.build(job);
        progress.accept(new InsightProgress("ANALYZING_CATEGORIES", "Analisando categorias e o período anterior..."));
        progress.accept(new InsightProgress("GENERATING_RECOMMENDATIONS", "Gerando recomendações..."));
        return provider.generate(data);
    }
}
