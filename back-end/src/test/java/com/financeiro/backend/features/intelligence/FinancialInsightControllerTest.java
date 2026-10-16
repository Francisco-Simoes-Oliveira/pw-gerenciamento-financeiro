package com.financeiro.backend.features.intelligence;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.financeiro.backend.common.exception.GlobalExceptionHandler;
import com.financeiro.backend.features.intelligence.controller.FinancialInsightController;
import com.financeiro.backend.features.intelligence.dto.response.InsightStartResponse;
import com.financeiro.backend.features.intelligence.exception.InvalidInsightTicketException;
import com.financeiro.backend.features.intelligence.service.FinancialInsightService;
import com.financeiro.backend.security.services.UserDetailsImpl;

class FinancialInsightControllerTest {
    private final FinancialInsightService service = mock(FinancialInsightService.class);
    private final UUID userId = UUID.randomUUID(), walletId = UUID.randomUUID(), analysisId = UUID.randomUUID();
    private MockMvc mvc;

    @BeforeEach void setup() {
        var user = new UserDetailsImpl(userId, "Leitor", "viewer@example.test", "", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new FinancialInsightController(service))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void startsWithAuthenticatedUserAndReturnsNoStoreTicket() throws Exception {
        var start = new InsightStartResponse(analysisId, "temporary-ticket", Instant.parse("2026-10-15T12:01:00Z"),
                "/api/ai/insights/" + analysisId + "/stream");
        when(service.start(any(), eq(userId))).thenReturn(start);

        mvc.perform(post("/api/ai/insights").contentType(MediaType.APPLICATION_JSON)
                .content("{\"walletId\":\"" + walletId + "\",\"startDate\":\"2026-10-01\",\"endDate\":\"2026-10-31\"}"))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.analysisId").value(analysisId.toString()))
                .andExpect(jsonPath("$.data.ticket").value("temporary-ticket"));
        verify(service).start(argThat(request -> request.walletId().equals(walletId)), eq(userId));
    }

    @Test void malformedRequestAndInvalidStreamTicketUseStandardErrors() throws Exception {
        mvc.perform(post("/api/ai/insights").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));

        when(service.openStream(analysisId, "bad")).thenThrow(new InvalidInsightTicketException());
        mvc.perform(get("/api/ai/insights/{id}/stream", analysisId).param("ticket", "bad"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.success").value(false));
    }
}
