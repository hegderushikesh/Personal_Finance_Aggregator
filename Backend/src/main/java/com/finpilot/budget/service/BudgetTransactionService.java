package com.finpilot.budget.service;

import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetTransactionService {

    private final BudgetRepository budgetRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final BudgetRolloverService budgetRolloverService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void recalculateCategoryActivity(User user, Category category, int year, int month) {
        if (category == null) return;

        YearMonth ym = YearMonth.of(year, month);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 1. Calculate actual sum of expenses for this category & month
        BigDecimal totalExpense = transactionRepository.sumAmountByUserAndCategoryAndTypeAndDateBetween(
                user, category, TransactionType.EXPENSE, startDate, endDate
        );
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        // 2. Find or create the Budget for that month
        Budget budget = budgetRepository.findByUserAndYearAndMonth(user, year, month)
                .orElseGet(() -> budgetRepository.save(Budget.builder()
                        .user(user)
                        .year(year)
                        .month(month)
                        .startingBalance(BigDecimal.ZERO)
                        .build()));

        // 3. Find or create BudgetAllocation
        BudgetAllocation allocation = budgetAllocationRepository.findByBudgetAndCategory(budget, category)
                .orElseGet(() -> BudgetAllocation.builder()
                        .budget(budget)
                        .category(category)
                        .assignedAmount(BigDecimal.ZERO)
                        .activityAmount(BigDecimal.ZERO)
                        .availableAmount(BigDecimal.ZERO)
                        .build());

        // 4. Update activity and available amounts
        allocation.setActivityAmount(totalExpense);

        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, year, month);
        BigDecimal rolledOver = rolledOverMap.getOrDefault(category.getId(), BigDecimal.ZERO);
        BigDecimal available = rolledOver.add(allocation.getAssignedAmount()).subtract(totalExpense);
        allocation.setAvailableAmount(available);

        budgetAllocationRepository.save(allocation);

        log.debug("Recalculated activity for user {} category {} ({}/{}): Activity={}, Available={}",
                user.getId(), category.getName(), month, year, totalExpense, available);
    }
}
