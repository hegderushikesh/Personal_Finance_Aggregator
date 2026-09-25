package com.finpilot.budget.service;

import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetRolloverService {

    private final BudgetRepository budgetRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;

    @Transactional(readOnly = true)
    public Map<Long, BigDecimal> getRolledOverAvailableBalances(User user, int year, int month) {
        int prevYear = month == 1 ? year - 1 : year;
        int prevMonth = month == 1 ? 12 : month - 1;

        Optional<Budget> prevBudgetOpt = budgetRepository.findByUserAndYearAndMonth(user, prevYear, prevMonth);
        Map<Long, BigDecimal> rolledOverMap = new HashMap<>();

        if (prevBudgetOpt.isPresent()) {
            List<BudgetAllocation> prevAllocations = budgetAllocationRepository.findByBudget(prevBudgetOpt.get());
            for (BudgetAllocation alloc : prevAllocations) {
                BigDecimal available = alloc.getAvailableAmount();
                if (available != null && available.compareTo(BigDecimal.ZERO) > 0) {
                    rolledOverMap.put(alloc.getCategory().getId(), available);
                }
            }
        }

        return rolledOverMap;
    }
}
