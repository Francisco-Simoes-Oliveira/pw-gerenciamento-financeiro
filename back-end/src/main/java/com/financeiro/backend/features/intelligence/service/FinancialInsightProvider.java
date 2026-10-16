package com.financeiro.backend.features.intelligence.service;

import com.financeiro.backend.features.intelligence.dto.response.InsightResult;
import com.financeiro.backend.features.intelligence.model.FinancialInsightData;

public interface FinancialInsightProvider {
    InsightResult generate(FinancialInsightData data);
}
