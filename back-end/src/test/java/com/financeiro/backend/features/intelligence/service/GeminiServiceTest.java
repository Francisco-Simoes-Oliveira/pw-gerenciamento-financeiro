package com.financeiro.backend.features.intelligence.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeiro.backend.features.intelligence.exception.InsightProviderException;
import com.financeiro.backend.features.intelligence.model.FinancialInsightData;

class GeminiServiceTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test void callsGenerateContentWithKeyAndParsesStructuredResponse() {
        var builder = RestClient.builder().baseUrl("https://gemini.example.test");
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://gemini.example.test/v1beta/models/gemini-test:generateContent"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-goog-api-key", "secret-test-key"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("responseSchema")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("userId"))))
                .andRespond(withSuccess("""
                        {"candidates":[{"content":{"parts":[{"text":"{\\"summary\\":\\"Saldo positivo.\\",\\"highlights\\":[\\"Despesas cresceram 10%.\\"],\\"recommendations\\":[\\"Revise alimentação.\\"]}"}]}}]}
                        """, MediaType.APPLICATION_JSON));
        var service = new GeminiService(mapper, builder.build(), "secret-test-key", "gemini-test");

        var result = service.generate(data());

        assertEquals("Saldo positivo.", result.summary());
        assertEquals(List.of("Despesas cresceram 10%."), result.highlights());
        assertEquals(List.of("Revise alimentação."), result.recommendations());
        assertTrue(result.disclaimer().contains("não constitui aconselhamento financeiro"));
        server.verify();
    }

    @Test void missingKeyAndInvalidProviderPayloadFailSafely() {
        assertThrows(InsightProviderException.class,
                () -> new GeminiService(mapper, RestClient.create(), "", "gemini-test").generate(data()));
        assertThrows(InsightProviderException.class,
                () -> new GeminiService(mapper, RestClient.create(), "key", "../../bad").generate(data()));
    }

    private FinancialInsightData data() {
        var current = new FinancialInsightData.PeriodTotals(new BigDecimal("5000"), new BigDecimal("3200"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1800"));
        var previous = new FinancialInsightData.PeriodTotals(new BigDecimal("4800"), new BigDecimal("2900"),
                BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("1900"));
        return new FinancialInsightData("2026-10-01", "2026-10-31", "BRL", current, previous,
                new BigDecimal("10.34"), 12,
                List.of(new FinancialInsightData.CategoryComparison("Alimentação", new BigDecimal("850"), new BigDecimal("700"))));
    }
}
