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
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final AccountRepository accountRepository;

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Resolves date range based on passed parameters.
     * Default behavior: current month if no range or month/year provided.
     */
    public DateRange resolveDateRange(LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                return new DateRange(endDate, startDate);
            }
            return new DateRange(startDate, endDate);
        }

        if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
            return new DateRange(start, end);
        }

        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year, 12, 31);
            return new DateRange(start, end);
        }

        // Default: current month
        LocalDate now = LocalDate.now();
        LocalDate start = now.withDayOfMonth(1);
        LocalDate end = now.withDayOfMonth(now.lengthOfMonth());
        return new DateRange(start, end);
    }

    /**
     * Spending breakdown by category for EXPENSE transactions.
     */
    public List<SpendingResponse> getSpending(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        DateRange range = resolveDateRange(startDate, endDate, month, year);
        List<SpendingProjection> projections = analyticsRepository.findSpendingByCategory(
                user.getId(), range.startDate(), range.endDate()
        );

        if (projections.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal totalSpending = projections.stream()
                .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return projections.stream()
                .map(p -> {
                    BigDecimal amount = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                    BigDecimal percentage = BigDecimal.ZERO;
                    if (totalSpending.compareTo(BigDecimal.ZERO) > 0) {
                        percentage = amount.multiply(BigDecimal.valueOf(100))
                                .divide(totalSpending, 2, RoundingMode.HALF_UP);
                    }
                    return SpendingResponse.builder()
                            .category(p.getCategory())
                            .amount(amount)
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Monthly spending trends for EXPENSE transactions.
     * Ensures all months within the range are represented chronologically.
     */
    public List<SpendingTrendResponse> getSpendingTrends(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        DateRange range = resolveDateRange(startDate, endDate, month, year);
        List<SpendingTrendProjection> projections = analyticsRepository.findSpendingTrends(
                user.getId(), range.startDate(), range.endDate()
        );

        Map<String, BigDecimal> monthToAmount = new HashMap<>();
        for (SpendingTrendProjection p : projections) {
            if (p.getMonth() != null) {
                monthToAmount.put(p.getMonth(), p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO);
            }
        }

        List<SpendingTrendResponse> result = new ArrayList<>();
        YearMonth current = YearMonth.from(range.startDate());
        YearMonth end = YearMonth.from(range.endDate());

        while (!current.isAfter(end)) {
            String monthKey = current.format(MONTH_FORMATTER);
            BigDecimal amount = monthToAmount.getOrDefault(monthKey, BigDecimal.ZERO);
            result.add(SpendingTrendResponse.builder()
                    .month(monthKey)
                    .amount(amount)
                    .build());
            current = current.plusMonths(1);
        }

        return result;
    }

    /**
     * Monthly income vs expense. Only INCOME and EXPENSE included; TRANSFER excluded.
     */
    public List<IncomeExpenseResponse> getIncomeExpense(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        DateRange range = resolveDateRange(startDate, endDate, month, year);
        List<IncomeExpenseProjection> projections = analyticsRepository.findMonthlyIncomeAndExpense(
                user.getId(), range.startDate(), range.endDate()
        );

        Map<String, IncomeExpenseProjection> monthMap = new HashMap<>();
        for (IncomeExpenseProjection p : projections) {
            if (p.getMonth() != null) {
                monthMap.put(p.getMonth(), p);
            }
        }

        List<IncomeExpenseResponse> result = new ArrayList<>();
        YearMonth current = YearMonth.from(range.startDate());
        YearMonth end = YearMonth.from(range.endDate());

        while (!current.isAfter(end)) {
            String monthKey = current.format(MONTH_FORMATTER);
            IncomeExpenseProjection p = monthMap.get(monthKey);
            BigDecimal income = p != null && p.getIncome() != null ? p.getIncome() : BigDecimal.ZERO;
            BigDecimal expense = p != null && p.getExpense() != null ? p.getExpense() : BigDecimal.ZERO;

            result.add(IncomeExpenseResponse.builder()
                    .month(monthKey)
                    .income(income)
                    .expense(expense)
                    .build());
            current = current.plusMonths(1);
        }

        return result;
    }

    /**
     * Monthly cash flow: netCashFlow = income - expense.
     */
    public List<CashFlowResponse> getCashFlow(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        List<IncomeExpenseResponse> incomeExpenses = getIncomeExpense(user, startDate, endDate, month, year);

        return incomeExpenses.stream()
                .map(ie -> CashFlowResponse.builder()
                        .month(ie.getMonth())
                        .income(ie.getIncome())
                        .expense(ie.getExpense())
                        .netCashFlow(ie.getIncome().subtract(ie.getExpense()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Detailed category spending sorted by amount DESC.
     */
    public List<CategorySpendingResponse> getCategorySpending(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        DateRange range = resolveDateRange(startDate, endDate, month, year);
        List<CategorySpendingProjection> projections = analyticsRepository.findCategorySpending(
                user.getId(), range.startDate(), range.endDate()
        );

        return projections.stream()
                .map(p -> CategorySpendingResponse.builder()
                        .categoryId(p.getCategoryId())
                        .category(p.getCategory())
                        .group(p.getCategoryGroup())
                        .categoryGroup(p.getCategoryGroup())
                        .amount(p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                        .transactionCount(p.getTransactionCount() != null ? p.getTransactionCount() : 0L)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Net worth calculation from active accounts: Assets - Liabilities.
     * Assets: CASH, CHECKING, SAVINGS, INVESTMENT, OTHER.
     * Liabilities: CREDIT_CARD.
     */
    public NetWorthResponse getNetWorth(User user) {
        List<Account> accounts = accountRepository.findByUserOrderByIsActiveDescTypeAscNameAsc(user);

        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal liabilities = BigDecimal.ZERO;

        for (Account acc : accounts) {
            if (!Boolean.TRUE.equals(acc.getIsActive())) {
                continue;
            }

            BigDecimal balance = acc.getBalance() != null ? acc.getBalance() : BigDecimal.ZERO;
            AccountType type = acc.getType() != null ? acc.getType() : AccountType.CHECKING;

            if (type == AccountType.CREDIT_CARD) {
                liabilities = liabilities.add(balance);
            } else {
                assets = assets.add(balance);
            }
        }

        BigDecimal netWorth = assets.subtract(liabilities);

        return NetWorthResponse.builder()
                .assets(assets)
                .liabilities(liabilities)
                .netWorth(netWorth)
                .build();
    }

    /**
     * Net worth trend note:
     * As specified in Step 8 Requirement 12, historical balance snapshots are not tracked in the current database schema.
     * To avoid inventing approximate historical balances as actual data, we report the verified current net worth for the current month.
     */
    public List<NetWorthTrendResponse> getNetWorthTrend(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        DateRange range = resolveDateRange(startDate, endDate, month, year);
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth startYearMonth = YearMonth.from(range.startDate());
        YearMonth endYearMonth = YearMonth.from(range.endDate());

        List<NetWorthTrendResponse> result = new ArrayList<>();
        if (!currentYearMonth.isBefore(startYearMonth) && !currentYearMonth.isAfter(endYearMonth)) {
            NetWorthResponse currentNetWorth = getNetWorth(user);
            result.add(NetWorthTrendResponse.builder()
                    .month(currentYearMonth.format(MONTH_FORMATTER))
                    .assets(currentNetWorth.getAssets())
                    .liabilities(currentNetWorth.getLiabilities())
                    .netWorth(currentNetWorth.getNetWorth())
                    .build());
        }

        return result;
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {}
}
