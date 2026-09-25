package com.finpilot.ai.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.analytics.dto.CashFlowResponse;
import com.finpilot.analytics.dto.IncomeExpenseResponse;
import com.finpilot.analytics.dto.NetWorthResponse;
import com.finpilot.analytics.dto.SpendingResponse;
import com.finpilot.analytics.service.AnalyticsService;
import com.finpilot.budget.dto.BudgetAllocationResponse;
import com.finpilot.budget.dto.BudgetResponse;
import com.finpilot.budget.service.BudgetService;
import com.finpilot.category.dto.CategoryResponse;
import com.finpilot.category.service.CategoryService;
import com.finpilot.recurring.dto.RecurringPaymentResponse;
import com.finpilot.recurring.service.RecurringPaymentService;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FinanceContextService {

    private final AnalyticsService analyticsService;
    private final BudgetService budgetService;
    private final RecurringPaymentService recurringPaymentService;
    private final CategoryService categoryService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    /**
     * Builds spending context for a specific period or current month.
     */
    public Map<String, Object> buildSpendingContext(User user, LocalDate startDate, LocalDate endDate, Integer month, Integer year) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        String period = LocalDate.of(targetYear, targetMonth, 1).format(PERIOD_FORMATTER);

        List<SpendingResponse> spendingList = analyticsService.getSpending(user, startDate, endDate, targetMonth, targetYear);
        BigDecimal totalSpending = spendingList.stream()
                .map(SpendingResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> categories = spendingList.stream()
                .map(s -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("category", sanitizeDataField(s.getCategory()));
                    item.put("amount", s.getAmount());
                    item.put("percentage", s.getPercentage() != null ? s.getPercentage() + "%" : "0%");
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("period", period);
        context.put("totalSpending", totalSpending);
        context.put("categoryCount", categories.size());
        if (!categories.isEmpty()) {
            context.put("topCategory", categories.get(0).get("category"));
            context.put("topCategoryAmount", categories.get(0).get("amount"));
        }
        context.put("spendingByCategory", categories);
        return context;
    }

    /**
     * Builds budget context for a specific month/year.
     */
    public Map<String, Object> buildBudgetContext(User user, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        String period = LocalDate.of(targetYear, targetMonth, 1).format(PERIOD_FORMATTER);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("period", period);

        try {
            BudgetResponse budget = budgetService.getOrCreateBudget(user, targetYear, targetMonth);
            context.put("readyToAssign", budget.getReadyToAssign());
            context.put("totalAssigned", budget.getTotalAssigned());
            context.put("totalActivity", budget.getTotalActivity());
            context.put("totalAvailable", budget.getTotalAvailable());

            if (budget.getBudgetHealth() != null) {
                Map<String, Object> health = new LinkedHashMap<>();
                health.put("fundedCount", budget.getBudgetHealth().getFundedCount());
                health.put("underfundedCount", budget.getBudgetHealth().getUnderfundedCount());
                health.put("overfundedCount", budget.getBudgetHealth().getOverfundedCount());
                health.put("targetsMetCount", budget.getBudgetHealth().getTargetsMetCount());
                context.put("budgetHealth", health);
            }

            List<Map<String, Object>> categorySummaries = new ArrayList<>();
            if (budget.getGroups() != null) {
                for (var group : budget.getGroups()) {
                    if (group.getCategories() != null) {
                        for (BudgetAllocationResponse alloc : group.getCategories()) {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("category", sanitizeDataField(alloc.getCategoryName()));
                            item.put("group", sanitizeDataField(alloc.getCategoryGroupName()));
                            item.put("assigned", alloc.getAssigned());
                            item.put("activity", alloc.getActivity());
                            item.put("available", alloc.getAvailable());
                            item.put("status", alloc.getStatus());
                            categorySummaries.add(item);
                        }
                    }
                }
            }
            context.put("categoryBudgets", categorySummaries);
        } catch (Exception e) {
            log.warn("Could not retrieve budget for user {}: {}", user.getId(), e.getMessage());
            context.put("error", "No active budget found for " + period);
        }

        return context;
    }

    /**
     * Builds recurring payments summary context.
     */
    public Map<String, Object> buildRecurringContext(User user) {
        List<RecurringPaymentResponse> recurringList = recurringPaymentService.getRecurringPayments(user, null, null, null);

        BigDecimal totalMonthlyEstimate = recurringList.stream()
                .map(r -> {
                    BigDecimal amt = r.getAverageAmount() != null ? r.getAverageAmount() : r.getLastAmount();
                    return amt != null ? amt : BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> items = recurringList.stream()
                .map(r -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("merchant", sanitizeDataField(r.getMerchantName()));
                    item.put("amount", r.getAverageAmount() != null ? r.getAverageAmount() : r.getLastAmount());
                    item.put("frequency", r.getFrequency() != null ? r.getFrequency().name() : "MONTHLY");
                    item.put("category", sanitizeDataField(r.getCategoryName()));
                    item.put("nextExpectedDate", r.getNextExpectedDate() != null ? r.getNextExpectedDate().toString() : "N/A");
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("totalRecurringCount", items.size());
        context.put("totalEstimatedMonthlyAmount", totalMonthlyEstimate);
        context.put("recurringPayments", items);
        return context;
    }

    /**
     * Builds comprehensive monthly financial summary context.
     */
    public Map<String, Object> buildMonthlySummaryContext(User user, Integer year, Integer month) {
        LocalDate now = LocalDate.now();
        int targetYear = (year != null) ? year : now.getYear();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        String period = LocalDate.of(targetYear, targetMonth, 1).format(PERIOD_FORMATTER);

        Map<String, Object> context = new LinkedHashMap<>();
        context.put("period", period);

        // 1. Income, Expenses, Cash Flow
        List<IncomeExpenseResponse> ieList = analyticsService.getIncomeExpense(user, null, null, targetMonth, targetYear);
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        if (!ieList.isEmpty()) {
            income = ieList.get(0).getIncome();
            expense = ieList.get(0).getExpense();
        }
        BigDecimal netCashFlow = income.subtract(expense);
        context.put("income", income);
        context.put("expenses", expense);
        context.put("netCashFlow", netCashFlow);
        context.put("savingsAmount", netCashFlow); // pre-calculated income - expenses
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal savingsRate = netCashFlow.multiply(BigDecimal.valueOf(100))
                    .divide(income, 2, java.math.RoundingMode.HALF_UP);
            context.put("savingsRatePercentage", savingsRate + "%");
        }

        // 2. Spending Top Categories
        List<SpendingResponse> spendingList = analyticsService.getSpending(user, null, null, targetMonth, targetYear);
        List<Map<String, Object>> topCategories = spendingList.stream()
                .limit(5)
                .map(s -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("category", sanitizeDataField(s.getCategory()));
                    item.put("amount", s.getAmount());
                    item.put("percentage", s.getPercentage() != null ? s.getPercentage() + "%" : "0%");
                    return item;
                })
                .collect(Collectors.toList());
        context.put("topSpendingCategories", topCategories);

        // 3. Recurring Payments Summary
        Map<String, Object> recurringCtx = buildRecurringContext(user);
        context.put("recurringCount", recurringCtx.get("totalRecurringCount"));
        context.put("recurringTotal", recurringCtx.get("totalEstimatedMonthlyAmount"));
        context.put("recurringPayments", recurringCtx.get("recurringPayments"));

        // 4. Net Worth Summary
        NetWorthResponse netWorth = analyticsService.getNetWorth(user);
        context.put("assets", netWorth.getAssets());
        context.put("liabilities", netWorth.getLiabilities());
        context.put("netWorth", netWorth.getNetWorth());

        return context;
    }

    /**
     * Builds contextual financial snapshot dynamically based on detected intent.
     */
    public Map<String, Object> buildIntentContext(User user, FinanceIntent intent) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        switch (intent) {
            case SPENDING -> {
                return buildSpendingContext(user, null, null, month, year);
            }
            case BUDGET -> {
                return buildBudgetContext(user, year, month);
            }
            case RECURRING -> {
                return buildRecurringContext(user);
            }
            case CASH_FLOW -> {
                List<CashFlowResponse> cf = analyticsService.getCashFlow(user, null, null, month, year);
                Map<String, Object> ctx = new LinkedHashMap<>();
                ctx.put("period", LocalDate.of(year, month, 1).format(PERIOD_FORMATTER));
                if (!cf.isEmpty()) {
                    BigDecimal inc = cf.get(0).getIncome();
                    BigDecimal exp = cf.get(0).getExpense();
                    BigDecimal netCashFlow = cf.get(0).getNetCashFlow();
                    ctx.put("income", inc);
                    ctx.put("expense", exp);
                    ctx.put("netCashFlow", netCashFlow);
                    ctx.put("savingsAmount", netCashFlow); // income - expenses pre-calculated
                    if (inc.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal savingsRate = netCashFlow.multiply(BigDecimal.valueOf(100))
                                .divide(inc, 2, java.math.RoundingMode.HALF_UP);
                        ctx.put("savingsRatePercentage", savingsRate + "%");
                    }
                } else {
                    ctx.put("income", BigDecimal.ZERO);
                    ctx.put("expense", BigDecimal.ZERO);
                    ctx.put("netCashFlow", BigDecimal.ZERO);
                    ctx.put("savingsAmount", BigDecimal.ZERO);
                }
                return ctx;
            }
            case NET_WORTH -> {
                NetWorthResponse nw = analyticsService.getNetWorth(user);
                Map<String, Object> ctx = new LinkedHashMap<>();
                ctx.put("assets", nw.getAssets());
                ctx.put("liabilities", nw.getLiabilities());
                ctx.put("netWorth", nw.getNetWorth());
                return ctx;
            }
            case TRANSACTIONS -> {
                List<Transaction> transactions = transactionRepository.findByUser(
                        user, PageRequest.of(0, 10)
                ).getContent();
                List<Map<String, Object>> txItems = transactions.stream()
                        .map(t -> {
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("date", t.getTransactionDate() != null ? t.getTransactionDate().toString() : "");
                            item.put("merchant", sanitizeDataField(t.getMerchantName()));
                            item.put("type", t.getType() != null ? t.getType().name() : "");
                            item.put("amount", t.getAmount());
                            item.put("category", t.getCategory() != null ? sanitizeDataField(t.getCategory().getName()) : "Uncategorized");
                            return item;
                        })
                        .collect(Collectors.toList());

                Map<String, Object> ctx = new LinkedHashMap<>();
                ctx.put("recentTransactions", txItems);
                return ctx;
            }
            case GENERAL_FINANCE -> {
                return buildMonthlySummaryContext(user, year, month);
            }
            default -> {
                return buildMonthlySummaryContext(user, year, month);
            }
        }
    }

    /**
     * Gets user's existing category names for category suggestion prompt.
     */
    public List<String> getUserCategoryNames(User user) {
        return categoryService.getUserCategories(user).stream()
                .map(CategoryResponse::getName)
                .filter(name -> name != null && !name.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Sanitizes user-input strings before sending to LLM context to prevent prompt injection.
     */
    private String sanitizeDataField(String input) {
        if (input == null) return "N/A";
        // Strip out control chars and backticks/quotes that might break context delimiting
        return input.replace("`", "'")
                .replace("\n", " ")
                .replace("\r", " ")
                .trim();
    }
}
