package com.finpilot.budget.service;

import com.finpilot.budget.dto.AssignMoneyRequest;
import com.finpilot.budget.dto.AutoAssignPreviewItem;
import com.finpilot.budget.dto.AutoAssignPreviewResponse;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.target.entity.Target;
import com.finpilot.target.repository.TargetRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AutoAssignService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final TargetRepository targetRepository;
    private final BudgetRolloverService budgetRolloverService;
    private final BudgetAllocationService budgetAllocationService;

    @Transactional(readOnly = true)
    public AutoAssignPreviewResponse generatePreview(User user, Budget budget, String mode) {
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);
        List<BudgetAllocation> currentAllocations = budgetAllocationRepository.findByBudget(budget);
        Map<Long, BudgetAllocation> currentAllocMap = new HashMap<>();
        currentAllocations.forEach(a -> currentAllocMap.put(a.getCategory().getId(), a));

        List<Target> targets = targetRepository.findByCategoryIn(categories);
        Map<Long, Target> targetMap = new HashMap<>();
        targets.forEach(t -> targetMap.put(t.getCategory().getId(), t));

        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, budget.getYear(), budget.getMonth());

        int prevYear = budget.getMonth() == 1 ? budget.getYear() - 1 : budget.getYear();
        int prevMonth = budget.getMonth() == 1 ? 12 : budget.getMonth() - 1;
        Optional<Budget> prevBudgetOpt = budgetRepository.findByUserAndYearAndMonth(user, prevYear, prevMonth);

        Map<Long, BudgetAllocation> prevAllocMap = new HashMap<>();
        if (prevBudgetOpt.isPresent()) {
            budgetAllocationRepository.findByBudget(prevBudgetOpt.get())
                    .forEach(a -> prevAllocMap.put(a.getCategory().getId(), a));
        }

        BigDecimal totalAssignedSoFar = currentAllocations.stream()
                .map(BudgetAllocation::getAssignedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal starting = budget.getStartingBalance() != null ? budget.getStartingBalance() : BigDecimal.ZERO;
        BigDecimal readyToAssignBefore = starting.subtract(totalAssignedSoFar);

        List<AutoAssignPreviewItem> previewItems = new ArrayList<>();
        BigDecimal totalRequired = BigDecimal.ZERO;

        for (Category category : categories) {
            Long catId = category.getId();
            BudgetAllocation currentAlloc = currentAllocMap.get(catId);
            BigDecimal currentAssigned = currentAlloc != null ? currentAlloc.getAssignedAmount() : BigDecimal.ZERO;
            BigDecimal currentAvailable = currentAlloc != null ? currentAlloc.getAvailableAmount() : rolledOverMap.getOrDefault(catId, BigDecimal.ZERO);

            BigDecimal targetAssigned = currentAssigned;

            if ("FUND_TARGETS".equalsIgnoreCase(mode) || "UNDERFUNDED".equalsIgnoreCase(mode)) {
                Target target = targetMap.get(catId);
                if (target != null && !Boolean.TRUE.equals(target.getSnoozed())) {
                    BigDecimal needed = target.getMonthlyAmount() != null ? target.getMonthlyAmount() : target.getAmount();
                    if (needed != null && needed.compareTo(currentAssigned) > 0) {
                        targetAssigned = needed;
                    }
                }
            } else if ("SAME_AS_LAST_MONTH".equalsIgnoreCase(mode)) {
                BudgetAllocation prevAlloc = prevAllocMap.get(catId);
                if (prevAlloc != null && prevAlloc.getAssignedAmount() != null) {
                    targetAssigned = prevAlloc.getAssignedAmount();
                }
            } else if ("LAST_MONTH_SPENDING".equalsIgnoreCase(mode)) {
                BudgetAllocation prevAlloc = prevAllocMap.get(catId);
                if (prevAlloc != null && prevAlloc.getActivityAmount() != null && prevAlloc.getActivityAmount().compareTo(BigDecimal.ZERO) > 0) {
                    targetAssigned = prevAlloc.getActivityAmount();
                }
            }

            BigDecimal diff = targetAssigned.subtract(currentAssigned);
            if (diff.compareTo(BigDecimal.ZERO) > 0) {
                totalRequired = totalRequired.add(diff);
                previewItems.add(AutoAssignPreviewItem.builder()
                        .categoryId(catId)
                        .categoryName(category.getName())
                        .currentAssigned(currentAssigned)
                        .additionalAmount(diff)
                        .newAssigned(targetAssigned)
                        .build());
            }
        }

        if ("REMAINING_TO_SAVINGS".equalsIgnoreCase(mode)) {
            Category savingsCategory = categories.stream()
                    .filter(c -> "Savings".equalsIgnoreCase(c.getCategoryGroup().getName()))
                    .findFirst()
                    .orElse(categories.isEmpty() ? null : categories.get(0));

            if (savingsCategory != null && readyToAssignBefore.compareTo(BigDecimal.ZERO) > 0) {
                Long catId = savingsCategory.getId();
                BudgetAllocation currentAlloc = currentAllocMap.get(catId);
                BigDecimal currentAssigned = currentAlloc != null ? currentAlloc.getAssignedAmount() : BigDecimal.ZERO;

                totalRequired = readyToAssignBefore;
                previewItems.clear();
                previewItems.add(AutoAssignPreviewItem.builder()
                        .categoryId(catId)
                        .categoryName(savingsCategory.getName())
                        .currentAssigned(currentAssigned)
                        .additionalAmount(readyToAssignBefore)
                        .newAssigned(currentAssigned.add(readyToAssignBefore))
                        .build());
            }
        }

        BigDecimal readyToAssignAfter = readyToAssignBefore.subtract(totalRequired);

        return AutoAssignPreviewResponse.builder()
                .mode(mode)
                .totalRequired(totalRequired)
                .readyToAssignAfter(readyToAssignAfter)
                .items(previewItems)
                .build();
    }

    @Transactional
    public void applyAutoAssign(User user, Budget budget, String mode) {
        AutoAssignPreviewResponse preview = generatePreview(user, budget, mode);
        for (AutoAssignPreviewItem item : preview.getItems()) {
            AssignMoneyRequest req = AssignMoneyRequest.builder()
                    .categoryId(item.getCategoryId())
                    .amount(item.getNewAssigned())
                    .build();
            budgetAllocationService.assignMoney(user, budget.getId(), req);
        }
    }
}
