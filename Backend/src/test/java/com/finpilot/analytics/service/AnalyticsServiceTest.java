package com.finpilot.analytics.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.analytics.dto.*;
import com.finpilot.analytics.repository.AnalyticsRepository;
import com.finpilot.analytics.repository.projection.CategorySpendingProjection;
import com.finpilot.analytics.repository.projection.IncomeExpenseProjection;
import com.finpilot.analytics.repository.projection.SpendingProjection;
import com.finpilot.analytics.repository.projection.SpendingTrendProjection;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .name("User One")
                .authProvider(AuthProvider.LOCAL)
                .build();

        user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .name("User Two")
                .authProvider(AuthProvider.LOCAL)
                .build();
    }

    private SpendingProjection createSpendingProjection(String category, BigDecimal amount) {
        return new SpendingProjection() {
            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public BigDecimal getAmount() {
                return amount;
            }
        };
    }

    private SpendingTrendProjection createTrendProjection(String month, BigDecimal amount) {
        return new SpendingTrendProjection() {
            @Override
            public String getMonth() {
                return month;
            }

            @Override
            public BigDecimal getAmount() {
                return amount;
            }
        };
    }

    private IncomeExpenseProjection createIncomeExpenseProjection(String month, BigDecimal income, BigDecimal expense) {
        return new IncomeExpenseProjection() {
            @Override
            public String getMonth() {
                return month;
            }

            @Override
            public BigDecimal getIncome() {
                return income;
            }

            @Override
            public BigDecimal getExpense() {
                return expense;
            }
        };
    }

    private CategorySpendingProjection createCategorySpendingProjection(Long id, String category, String group, BigDecimal amount, Long count) {
        return new CategorySpendingProjection() {
            @Override
            public Long getCategoryId() {
                return id;
            }

            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public String getCategoryGroup() {
                return group;
            }

            @Override
            public BigDecimal getAmount() {
                return amount;
            }

            @Override
            public Long getTransactionCount() {
                return count;
            }
        };
    }

    @Test
    @DisplayName("1. Spending groups EXPENSE by category and calculates percentages")
    void testSpendingGroupsExpenseByCategory() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        List<SpendingProjection> projections = List.of(
                createSpendingProjection("Food", new BigDecimal("8500.00")),
                createSpendingProjection("Bills", new BigDecimal("9000.00")),
                createSpendingProjection("Transport", new BigDecimal("4200.00"))
        );

        when(analyticsRepository.findSpendingByCategory(user1.getId(), start, end))
                .thenReturn(projections);

        List<SpendingResponse> result = analyticsService.getSpending(user1, start, end, null, null);

        assertEquals(3, result.size());
        assertEquals("Food", result.get(0).getCategory());
        assertEquals(new BigDecimal("8500.00"), result.get(0).getAmount());
        // Total = 21700. Food % = 8500 / 21700 * 100 = 39.17%
        assertEquals(new BigDecimal("39.17"), result.get(0).getPercentage());
    }

    @Test
    @DisplayName("2 & 3. User ownership is enforced - user1 ID is passed to repository")
    void testUserOwnershipEnforced() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(analyticsRepository.findSpendingByCategory(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        analyticsService.getSpending(user1, start, end, null, null);

        verify(analyticsRepository).findSpendingByCategory(eq(1L), eq(start), eq(end));
        verify(analyticsRepository, never()).findSpendingByCategory(eq(2L), any(), any());
    }

    @Test
    @DisplayName("4 & 5. Income and Expense grouped correctly in Monthly Income vs Expense")
    void testIncomeExpenseGroupedCorrectly() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);

        List<IncomeExpenseProjection> projections = List.of(
                createIncomeExpenseProjection("2026-01", new BigDecimal("60000.00"), new BigDecimal("42000.00")),
                createIncomeExpenseProjection("2026-02", new BigDecimal("60000.00"), new BigDecimal("38000.00"))
        );

        when(analyticsRepository.findMonthlyIncomeAndExpense(user1.getId(), start, end))
                .thenReturn(projections);

        List<IncomeExpenseResponse> result = analyticsService.getIncomeExpense(user1, start, end, null, null);

        assertEquals(2, result.size());
        assertEquals("2026-01", result.get(0).getMonth());
        assertEquals(new BigDecimal("60000.00"), result.get(0).getIncome());
        assertEquals(new BigDecimal("42000.00"), result.get(0).getExpense());
        assertEquals("2026-02", result.get(1).getMonth());
        assertEquals(new BigDecimal("60000.00"), result.get(1).getIncome());
        assertEquals(new BigDecimal("38000.00"), result.get(1).getExpense());
    }

    @Test
    @DisplayName("6. Cash flow = income - expense")
    void testCashFlowCalculation() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 2, 28);

        List<IncomeExpenseProjection> projections = List.of(
                createIncomeExpenseProjection("2026-01", new BigDecimal("60000.00"), new BigDecimal("42000.00")),
                createIncomeExpenseProjection("2026-02", new BigDecimal("60000.00"), new BigDecimal("38000.00"))
        );

        when(analyticsRepository.findMonthlyIncomeAndExpense(user1.getId(), start, end))
                .thenReturn(projections);

        List<CashFlowResponse> result = analyticsService.getCashFlow(user1, start, end, null, null);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("18000.00"), result.get(0).getNetCashFlow());
        assertEquals(new BigDecimal("22000.00"), result.get(1).getNetCashFlow());
    }

    @Test
    @DisplayName("7. Category spending sorted correctly and maps group/count")
    void testCategorySpendingSortedCorrectly() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        List<CategorySpendingProjection> projections = List.of(
                createCategorySpendingProjection(1L, "Food", "Needs", new BigDecimal("8500.00"), 18L),
                createCategorySpendingProjection(2L, "Transport", "Needs", new BigDecimal("4200.00"), 12L)
        );

        when(analyticsRepository.findCategorySpending(user1.getId(), start, end))
                .thenReturn(projections);

        List<CategorySpendingResponse> result = analyticsService.getCategorySpending(user1, start, end, null, null);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getCategoryId());
        assertEquals("Food", result.get(0).getCategory());
        assertEquals("Needs", result.get(0).getGroup());
        assertEquals(new BigDecimal("8500.00"), result.get(0).getAmount());
        assertEquals(18L, result.get(0).getTransactionCount());
    }

    @Test
    @DisplayName("8 & 9. Date and Month filtering resolution")
    void testDateRangeResolution() {
        // Month and year specified
        AnalyticsService.DateRange monthRange = analyticsService.resolveDateRange(null, null, 9, 2026);
        assertEquals(LocalDate.of(2026, 9, 1), monthRange.startDate());
        assertEquals(LocalDate.of(2026, 9, 30), monthRange.endDate());

        // Year only specified
        AnalyticsService.DateRange yearRange = analyticsService.resolveDateRange(null, null, null, 2026);
        assertEquals(LocalDate.of(2026, 1, 1), yearRange.startDate());
        assertEquals(LocalDate.of(2026, 12, 31), yearRange.endDate());

        // Explicit start and end date
        AnalyticsService.DateRange explicitRange = analyticsService.resolveDateRange(
                LocalDate.of(2026, 5, 10), LocalDate.of(2026, 6, 20), null, null
        );
        assertEquals(LocalDate.of(2026, 5, 10), explicitRange.startDate());
        assertEquals(LocalDate.of(2026, 6, 20), explicitRange.endDate());

        // Default: current month
        AnalyticsService.DateRange defaultRange = analyticsService.resolveDateRange(null, null, null, null);
        LocalDate now = LocalDate.now();
        assertEquals(now.withDayOfMonth(1), defaultRange.startDate());
        assertEquals(now.withDayOfMonth(now.lengthOfMonth()), defaultRange.endDate());
    }

    @Test
    @DisplayName("10 & 11. User A cannot see User B analytics")
    void testUserIsolation() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        analyticsService.getSpending(user1, start, end, null, null);
        verify(analyticsRepository).findSpendingByCategory(eq(1L), any(), any());

        analyticsService.getSpending(user2, start, end, null, null);
        verify(analyticsRepository).findSpendingByCategory(eq(2L), any(), any());
    }

    @Test
    @DisplayName("12. Net worth calculates assets minus liabilities using Account records")
    void testNetWorthCalculation() {
        List<Account> accounts = List.of(
                Account.builder().id(1L).user(user1).name("Cash").type(AccountType.CASH)
                        .balance(new BigDecimal("25000.00")).isActive(true).build(),
                Account.builder().id(2L).user(user1).name("Savings").type(AccountType.SAVINGS)
                        .balance(new BigDecimal("50000.00")).isActive(true).build(),
                Account.builder().id(3L).user(user1).name("Credit Card").type(AccountType.CREDIT_CARD)
                        .balance(new BigDecimal("10000.00")).isActive(true).build(),
                Account.builder().id(4L).user(user1).name("Inactive").type(AccountType.CHECKING)
                        .balance(new BigDecimal("5000.00")).isActive(false).build() // Inactive should be ignored
        );

        when(accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user1))
                .thenReturn(accounts);

        NetWorthResponse response = analyticsService.getNetWorth(user1);

        // Assets = 25000 + 50000 = 75000
        assertEquals(new BigDecimal("75000.00"), response.getAssets());
        // Liabilities = 10000
        assertEquals(new BigDecimal("10000.00"), response.getLiabilities());
        // Net Worth = 75000 - 10000 = 65000
        assertEquals(new BigDecimal("65000.00"), response.getNetWorth());
    }

    @Test
    @DisplayName("13. Empty transaction period returns empty results")
    void testEmptyTransactionPeriod() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(analyticsRepository.findSpendingByCategory(user1.getId(), start, end))
                .thenReturn(Collections.emptyList());

        List<SpendingResponse> result = analyticsService.getSpending(user1, start, end, null, null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("14. Uncategorized transactions are handled")
    void testUncategorizedTransactionsHandled() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        List<SpendingProjection> projections = List.of(
                createSpendingProjection("Uncategorized", new BigDecimal("1500.00"))
        );

        when(analyticsRepository.findSpendingByCategory(user1.getId(), start, end))
                .thenReturn(projections);

        List<SpendingResponse> result = analyticsService.getSpending(user1, start, end, null, null);
        assertEquals(1, result.size());
        assertEquals("Uncategorized", result.get(0).getCategory());
        assertEquals(new BigDecimal("1500.00"), result.get(0).getAmount());
        assertEquals(new BigDecimal("100.00"), result.get(0).getPercentage());
    }

    @Test
    @DisplayName("15. BigDecimal calculations are accurate and safe")
    void testBigDecimalCalculationsAccurate() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        List<SpendingProjection> projections = List.of(
                createSpendingProjection("Cat A", new BigDecimal("33.33")),
                createSpendingProjection("Cat B", new BigDecimal("66.67"))
        );

        when(analyticsRepository.findSpendingByCategory(user1.getId(), start, end))
                .thenReturn(projections);

        List<SpendingResponse> result = analyticsService.getSpending(user1, start, end, null, null);
        // Total = 100.00
        assertEquals(new BigDecimal("33.33"), result.get(0).getPercentage());
        assertEquals(new BigDecimal("66.67"), result.get(1).getPercentage());
    }

    @Test
    @DisplayName("16. Spending Trends timeline fills missing months with 0")
    void testSpendingTrendsFillsMissingMonths() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        // Only Feb has data
        List<SpendingTrendProjection> projections = List.of(
                createTrendProjection("2026-02", new BigDecimal("28500.00"))
        );

        when(analyticsRepository.findSpendingTrends(user1.getId(), start, end))
                .thenReturn(projections);

        List<SpendingTrendResponse> result = analyticsService.getSpendingTrends(user1, start, end, null, null);

        assertEquals(3, result.size());
        assertEquals("2026-01", result.get(0).getMonth());
        assertEquals(BigDecimal.ZERO, result.get(0).getAmount());

        assertEquals("2026-02", result.get(1).getMonth());
        assertEquals(new BigDecimal("28500.00"), result.get(1).getAmount());

        assertEquals("2026-03", result.get(2).getMonth());
        assertEquals(BigDecimal.ZERO, result.get(2).getAmount());
    }
}
