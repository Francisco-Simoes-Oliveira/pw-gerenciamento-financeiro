package com.financeiro.backend.features.reports.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.reports.dto.response.DashboardSummaryResponse;
import com.financeiro.backend.features.reports.dto.response.StatementResponse;
import com.financeiro.backend.features.reports.projection.DashboardProjection;
import com.financeiro.backend.features.reports.repository.CategoryReportRepository;
import com.financeiro.backend.features.reports.repository.DashboardRepository;
import com.financeiro.backend.features.reports.repository.IndicatorRepository;
import com.financeiro.backend.features.reports.repository.StatementRepository;
import com.financeiro.backend.features.transaction.entity.Transaction;

@ExtendWith(MockitoExtension.class)
class FinancialReportServiceImplTest {

    @Mock
    private DashboardRepository dashboardRepository;
    
    @Mock
    private StatementRepository statementRepository;
    
    @Mock
    private IndicatorRepository indicatorRepository;
    
    @Mock
    private CategoryReportRepository categoryReportRepository;

    @InjectMocks
    private FinancialReportServiceImpl reportService;

    private UUID userId;
    private ReportFilter filter;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        filter = new ReportFilter();
    }

    @Test
    void testGetDashboardSummary_ReturnsAggregatedData() {
        DashboardProjection proj = new DashboardProjection() {
            @Override public BigDecimal getIncome() { return BigDecimal.valueOf(5000); }
            @Override public BigDecimal getExpense() { return BigDecimal.valueOf(2000); }
            @Override public BigDecimal getTransfer() { return BigDecimal.valueOf(500); }
            @Override public Long getTransactionsCount() { return 10L; }
        };

        when(dashboardRepository.getDashboardConsolidated(userId, null)).thenReturn(proj);

        DashboardSummaryResponse response = reportService.getDashboardSummary(filter, userId);

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(5000).compareTo(response.getMonthlyIncome()));
        assertEquals(0, BigDecimal.valueOf(2000).compareTo(response.getMonthlyExpense()));
        assertEquals(0, BigDecimal.valueOf(3000).compareTo(response.getCurrentBalance())); // 5000 - 2000
        assertEquals(10L, response.getTransactions());
        
        // Assegurando que a consulta foi diretamente pro banco ao invés de findaAll()
        verify(dashboardRepository, times(1)).getDashboardConsolidated(userId, null);
    }
    
    @Test
    void testGetStatement_UsesSpecificationsAndPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        
        Transaction t = new Transaction();
        t.setId(UUID.randomUUID());
        t.setAmount(BigDecimal.valueOf(100));
        
        Page<Transaction> page = new PageImpl<>(List.of(t));
        
        when(statementRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable))).thenReturn(page);
        
        Page<StatementResponse> response = reportService.getStatement(filter, pageable, userId);
        
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(0, BigDecimal.valueOf(100).compareTo(response.getContent().get(0).getAmount()));
        
        verify(statementRepository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
    }
}
