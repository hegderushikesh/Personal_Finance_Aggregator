package com.finpilot.analytics.controller;

import com.finpilot.analytics.dto.*;
import com.finpilot.analytics.service.AnalyticsService;
import com.finpilot.security.UserPrincipal;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.Role;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private UserPrincipal testUserPrincipal;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("FinPilot User")
                .email("user@finpilot.com")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.USER)
                .build();

        testUserPrincipal = UserPrincipal.create(testUser);
    }

    @Test
    @DisplayName("GET /api/analytics/spending returns 200 OK when authenticated")
    void testGetSpending_Authenticated() {
        List<SpendingResponse> mockList = List.of(
                SpendingResponse.builder().category("Food").amount(new BigDecimal("8500")).percentage(new BigDecimal("50.00")).build()
        );
        when(analyticsService.getSpending(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockList);

        ResponseEntity<List<SpendingResponse>> response = analyticsController.getSpending(
                testUserPrincipal, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Food", response.getBody().get(0).getCategory());
    }

    @Test
    @DisplayName("GET /api/analytics/spending returns 401 UNAUTHORIZED when principal is null")
    void testGetSpending_Unauthenticated() {
        ResponseEntity<List<SpendingResponse>> response = analyticsController.getSpending(
                null, null, null, null, null
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    @DisplayName("GET /api/analytics/spending/trends returns 200 OK")
    void testGetSpendingTrends() {
        List<SpendingTrendResponse> mockTrends = List.of(
                SpendingTrendResponse.builder().month("2026-09").amount(new BigDecimal("21700")).build()
        );
        when(analyticsService.getSpendingTrends(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockTrends);

        ResponseEntity<List<SpendingTrendResponse>> response = analyticsController.getSpendingTrends(
                testUserPrincipal, null, null, 9, 2026
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("2026-09", response.getBody().get(0).getMonth());
    }

    @Test
    @DisplayName("GET /api/analytics/income-expense returns 200 OK")
    void testGetIncomeExpense() {
        List<IncomeExpenseResponse> mockData = List.of(
                IncomeExpenseResponse.builder().month("2026-09").income(new BigDecimal("60000")).expense(new BigDecimal("21700")).build()
        );
        when(analyticsService.getIncomeExpense(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockData);

        ResponseEntity<List<IncomeExpenseResponse>> response = analyticsController.getIncomeExpense(
                testUserPrincipal, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(new BigDecimal("60000"), response.getBody().get(0).getIncome());
    }

    @Test
    @DisplayName("GET /api/analytics/cash-flow returns 200 OK")
    void testGetCashFlow() {
        List<CashFlowResponse> mockCashFlow = List.of(
                CashFlowResponse.builder().month("2026-09").income(new BigDecimal("60000")).expense(new BigDecimal("21700")).netCashFlow(new BigDecimal("38300")).build()
        );
        when(analyticsService.getCashFlow(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockCashFlow);

        ResponseEntity<List<CashFlowResponse>> response = analyticsController.getCashFlow(
                testUserPrincipal, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("38300"), response.getBody().get(0).getNetCashFlow());
    }

    @Test
    @DisplayName("GET /api/analytics/categories returns 200 OK")
    void testGetCategories() {
        List<CategorySpendingResponse> mockCategories = List.of(
                CategorySpendingResponse.builder().categoryId(1L).category("Food").group("Needs").amount(new BigDecimal("8500")).transactionCount(18).build()
        );
        when(analyticsService.getCategorySpending(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockCategories);

        ResponseEntity<List<CategorySpendingResponse>> response = analyticsController.getCategories(
                testUserPrincipal, null, null, null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Needs", response.getBody().get(0).getGroup());
    }

    @Test
    @DisplayName("GET /api/analytics/net-worth returns 200 OK")
    void testGetNetWorth() {
        NetWorthResponse mockNetWorth = NetWorthResponse.builder()
                .assets(new BigDecimal("75000"))
                .liabilities(new BigDecimal("10000"))
                .netWorth(new BigDecimal("65000"))
                .build();
        when(analyticsService.getNetWorth(testUser)).thenReturn(mockNetWorth);

        ResponseEntity<NetWorthResponse> response = analyticsController.getNetWorth(testUserPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(new BigDecimal("65000"), response.getBody().getNetWorth());
    }

    @Test
    @DisplayName("GET /api/analytics/net-worth?startDate=... returns trend 200 OK")
    void testGetNetWorthTrend() {
        List<NetWorthTrendResponse> mockTrends = List.of(
                NetWorthTrendResponse.builder().month("2026-09").assets(new BigDecimal("75000")).liabilities(new BigDecimal("10000")).netWorth(new BigDecimal("65000")).build()
        );
        when(analyticsService.getNetWorthTrend(eq(testUser), any(), any(), any(), any()))
                .thenReturn(mockTrends);

        ResponseEntity<List<NetWorthTrendResponse>> response = analyticsController.getNetWorthWithDateRange(
                testUserPrincipal, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30), null, null
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }
}
