package com.finpilot.budget.service;

import com.finpilot.budget.dto.AssignMoneyRequest;
import com.finpilot.budget.dto.MoveMoneyRequest;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.entity.BudgetActivityType;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetAllocationService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final BudgetRolloverService budgetRolloverService;
    private final BudgetActivityService budgetActivityService;

    @Transactional
    public void assignMoney(User user, Long budgetId, AssignMoneyRequest request) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        Category category = categoryRepository.findByIdAndUser(request.getCategoryId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        BudgetAllocation allocation = budgetAllocationRepository.findByBudgetAndCategory(budget, category)
                .orElseGet(() -> BudgetAllocation.builder()
                        .budget(budget)
                        .category(category)
                        .assignedAmount(BigDecimal.ZERO)
                        .activityAmount(BigDecimal.ZERO)
                        .availableAmount(BigDecimal.ZERO)
                        .build());

        BigDecimal oldAssigned = allocation.getAssignedAmount();
        BigDecimal newAssigned = request.getAmount() != null ? request.getAmount() : BigDecimal.ZERO;
        BigDecimal difference = newAssigned.subtract(oldAssigned);

        allocation.setAssignedAmount(newAssigned);

        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, budget.getYear(), budget.getMonth());
        BigDecimal rolledOver = rolledOverMap.getOrDefault(category.getId(), BigDecimal.ZERO);
        allocation.setAvailableAmount(rolledOver.add(newAssigned).subtract(allocation.getActivityAmount()));

        budgetAllocationRepository.save(allocation);

        BudgetActivityType type = difference.compareTo(BigDecimal.ZERO) >= 0 ? BudgetActivityType.ASSIGN : BudgetActivityType.UNASSIGN;
        budgetActivityService.logActivity(
                user, budget, type, null, category, difference.abs(),
                "Updated assigned amount for " + category.getName() + " to ₹" + newAssigned
        );
    }

    @Transactional
    public void moveMoney(User user, Long budgetId, MoveMoneyRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Move amount must be greater than zero");
        }

        if (request.getFromCategoryId().equals(request.getToCategoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Source and destination categories must be different");
        }

        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        Category sourceCategory = categoryRepository.findByIdAndUser(request.getFromCategoryId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source category not found"));

        Category destCategory = categoryRepository.findByIdAndUser(request.getToCategoryId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination category not found"));

        BudgetAllocation sourceAlloc = budgetAllocationRepository.findByBudgetAndCategory(budget, sourceCategory)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source category allocation not found in this budget"));

        BudgetAllocation destAlloc = budgetAllocationRepository.findByBudgetAndCategory(budget, destCategory)
                .orElseGet(() -> BudgetAllocation.builder()
                        .budget(budget)
                        .category(destCategory)
                        .assignedAmount(BigDecimal.ZERO)
                        .activityAmount(BigDecimal.ZERO)
                        .availableAmount(BigDecimal.ZERO)
                        .build());

        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, budget.getYear(), budget.getMonth());

        BigDecimal moveAmount = request.getAmount();

        // Update Source Category
        sourceAlloc.setAssignedAmount(sourceAlloc.getAssignedAmount().subtract(moveAmount));
        BigDecimal sourceRolledOver = rolledOverMap.getOrDefault(sourceCategory.getId(), BigDecimal.ZERO);
        sourceAlloc.setAvailableAmount(sourceRolledOver.add(sourceAlloc.getAssignedAmount()).subtract(sourceAlloc.getActivityAmount()));

        // Update Dest Category
        destAlloc.setAssignedAmount(destAlloc.getAssignedAmount().add(moveAmount));
        BigDecimal destRolledOver = rolledOverMap.getOrDefault(destCategory.getId(), BigDecimal.ZERO);
        destAlloc.setAvailableAmount(destRolledOver.add(destAlloc.getAssignedAmount()).subtract(destAlloc.getActivityAmount()));

        budgetAllocationRepository.save(sourceAlloc);
        budgetAllocationRepository.save(destAlloc);

        budgetActivityService.logActivity(
                user, budget, BudgetActivityType.MOVE, sourceCategory, destCategory, moveAmount,
                "Moved ₹" + moveAmount + " from " + sourceCategory.getName() + " to " + destCategory.getName()
        );
    }
}
