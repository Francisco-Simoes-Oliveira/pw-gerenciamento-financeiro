package com.financeiro.backend.features.intelligence.dto.response;

import java.util.List;

public record InsightResult(String summary, List<String> highlights, List<String> recommendations, String disclaimer) {}
