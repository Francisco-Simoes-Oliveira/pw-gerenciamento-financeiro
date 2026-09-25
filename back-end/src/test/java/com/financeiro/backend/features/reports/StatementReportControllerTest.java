package com.financeiro.backend.features.reports;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
import com.financeiro.backend.features.reports.controller.StatementReportController;
import com.financeiro.backend.features.reports.service.StatementExportService;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.security.services.UserDetailsImpl;

class StatementReportControllerTest {
    StatementReportService service = mock(StatementReportService.class);
    MockMvc mvc;
    UUID userId = UUID.randomUUID(), walletId = UUID.randomUUID();
    @BeforeEach void setup() {
        var user = new UserDetailsImpl(userId, "Leitor", "viewer@example.test", "", true, List.of());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new StatementReportController(service, new StatementExportService()))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @AfterEach void cleanup() { SecurityContextHolder.clearContext(); }

    @Test void csvAndPdfUseAuthenticatedUserAndDownloadHeaders() throws Exception {
        when(service.generate(eq(walletId), any(), any(), eq(userId))).thenReturn(StatementExportServiceTest.fixture(List.of()));
        for (String format : List.of("csv", "pdf")) {
            mvc.perform(get("/api/reports/statement/export").param("walletId", walletId.toString()).param("format", format)
                    .param("startDate", "2026-09-01").param("endDate", "2026-09-30").param("userId", UUID.randomUUID().toString()))
                    .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"))
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"extrato-2026-09-01-2026-09-30." + format + "\""))
                    .andExpect(content().contentTypeCompatibleWith(format.equals("pdf") ? MediaType.APPLICATION_PDF : MediaType.parseMediaType("text/csv")));
        }
        verify(service, times(2)).generate(walletId, java.time.LocalDate.of(2026, 9, 1), java.time.LocalDate.of(2026, 9, 30), userId);
    }

    @Test void malformedFiltersAndUnsupportedFormatReturn400() throws Exception {
        mvc.perform(get("/api/reports/statement/report")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/statement/report").param("walletId", "invalid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/statement/report").param("walletId", walletId.toString()).param("startDate", "not-a-date")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/reports/statement/export").param("walletId", walletId.toString()).param("format", "exe")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test void deniedAccessReturns403ForScreenAndExports() throws Exception {
        when(service.generate(eq(walletId), any(), any(), eq(userId))).thenThrow(new org.springframework.security.access.AccessDeniedException("Sem acesso"));
        mvc.perform(get("/api/reports/statement/report").param("walletId", walletId.toString())).andExpect(status().isForbidden());
        for (String format : List.of("pdf", "csv")) {
            mvc.perform(get("/api/reports/statement/export").param("walletId", walletId.toString()).param("format", format)).andExpect(status().isForbidden());
        }
    }
}
