package com.finpilot.budget.service;

import com.finpilot.budget.dto.*;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.entity.BudgetActivity;
import com.finpilot.budget.entity.BudgetActivityType;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.category.repository.CategoryGroupRepository;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.category.service.CategoryGroupService;
import com.finpilot.target.dto.TargetResponse;
import com.finpilot.target.entity.Target;
import com.finpilot.target.repository.TargetRepository;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.finpilot.transaction.dto.TransactionResponse;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;

import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetAllocationRepository budgetAllocationRepository;
    private final CategoryGroupRepository categoryGroupRepository;
    private final CategoryRepository categoryRepository;
    private final TargetRepository targetRepository;
    private final CategoryGroupService categoryGroupService;
    private final BudgetRolloverService budgetRolloverService;
    private final BudgetActivityService budgetActivityService;
    private final BudgetAllocationService budgetAllocationService;
    private final TransactionRepository transactionRepository;

    @Transactional
    public BudgetResponse getOrCreateBudget(User user, Integer year, Integer month) {
        if (year == null || month == null) {
            YearMonth now = YearMonth.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        // Ensure default category template is populated for brand new users
        categoryGroupService.createDefaultCategoryTemplateIfEmpty(user);

        final int reqYear = year;
        final int reqMonth = month;

        Budget budget = budgetRepository.findByUserAndYearAndMonth(user, reqYear, reqMonth)
                .orElseGet(() -> budgetRepository.save(Budget.builder()
                        .user(user)
                        .year(reqYear)
                        .month(reqMonth)
                        .startingBalance(BigDecimal.ZERO)
                        .build()));

        return buildBudgetResponse(user, budget);
    }

    @Transactional
    public BudgetResponse setStartingBalance(User user, Long budgetId, BigDecimal startingBalance) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        budget.setStartingBalance(startingBalance != null ? startingBalance : BigDecimal.ZERO);
        budgetRepository.save(budget);

        budgetActivityService.logActivity(
                user, budget, BudgetActivityType.ASSIGN, null, null, startingBalance,
                "Updated starting available cash to ₹" + startingBalance
        );

        return buildBudgetResponse(user, budget);
    }

    @Transactional(readOnly = true)
    public CategoryDetailsResponse getCategoryDetails(User user, Long categoryId, Integer year, Integer month) {
        Category category = categoryRepository.findByIdAndUser(categoryId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        if (year == null || month == null) {
            YearMonth now = YearMonth.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        Budget budget = budgetRepository.findByUserAndYearAndMonth(user, year, month).orElse(null);
        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, year, month);
        BigDecimal cashLeftFromLastMonth = rolledOverMap.getOrDefault(categoryId, BigDecimal.ZERO);

        BigDecimal assignedThisMonth = BigDecimal.ZERO;
        BigDecimal activity = BigDecimal.ZERO;
        BigDecimal available = cashLeftFromLastMonth;

        if (budget != null) {
            BudgetAllocation alloc = budgetAllocationRepository.findByBudgetAndCategory(budget, category).orElse(null);
            if (alloc != null) {
                assignedThisMonth = alloc.getAssignedAmount();
                activity = alloc.getActivityAmount();
                available = alloc.getAvailableAmount();
            }
        }

        Target target = targetRepository.findByCategory(category).orElse(null);
        TargetResponse targetResp = TargetResponse.buildFromTargetAndAvailable(target, available, YearMonth.of(year, month));

        List<TransactionResponse> recentTransactions = transactionRepository
                .findTop10ByUserAndCategoryOrderByTransactionDateDescCreatedAtDesc(user, category)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());

        return CategoryDetailsResponse.builder()
                .categoryId(category.getId())
                .categoryName(category.getName())
                .categoryGroupName(category.getCategoryGroup().getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .cashLeftFromLastMonth(cashLeftFromLastMonth)
                .assignedThisMonth(assignedThisMonth)
                .activity(activity)
                .available(available)
                .target(targetResp)
                .note(category.getNote())
                .recentTransactions(recentTransactions)
                .build();
    }

    @Transactional
    public BudgetResponse copyPreviousMonth(User user, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        int prevYear = budget.getMonth() == 1 ? budget.getYear() - 1 : budget.getYear();
        int prevMonth = budget.getMonth() == 1 ? 12 : budget.getMonth() - 1;

        Budget prevBudget = budgetRepository.findByUserAndYearAndMonth(user, prevYear, prevMonth)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No previous month budget exists to copy"));

        List<BudgetAllocation> prevAllocations = budgetAllocationRepository.findByBudget(prevBudget);
        for (BudgetAllocation prevAlloc : prevAllocations) {
            AssignMoneyRequest req = AssignMoneyRequest.builder()
                    .categoryId(prevAlloc.getCategory().getId())
                    .amount(prevAlloc.getAssignedAmount())
                    .build();
            budgetAllocationService.assignMoney(user, budget.getId(), req);
        }

        budgetActivityService.logActivity(
                user, budget, BudgetActivityType.AUTO_ASSIGN, null, null, BigDecimal.ZERO,
                "Copied planned allocations from previous month (" + prevMonth + "/" + prevYear + ")"
        );

        return buildBudgetResponse(user, budget);
    }

    @Transactional
    public BudgetResponse undoLastActivity(User user, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Budget not found"));

        Optional<BudgetActivity> lastActivityOpt = budgetActivityService.getLastActivity(user, budget);
        if (lastActivityOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No recent move available to undo");
        }

        BudgetActivity lastActivity = lastActivityOpt.get();

        if (lastActivity.getType() == BudgetActivityType.MOVE && lastActivity.getSourceCategory() != null && lastActivity.getDestinationCategory() != null) {
            // Revert move: move amount back from destination to source
            MoveMoneyRequest revertReq = MoveMoneyRequest.builder()
                    .fromCategoryId(lastActivity.getDestinationCategory().getId())
                    .toCategoryId(lastActivity.getSourceCategory().getId())
                    .amount(lastActivity.getAmount())
                    .build();
            budgetAllocationService.moveMoney(user, budget.getId(), revertReq);
            // Delete both the last activity and the revert activity to maintain clean state
            budgetActivityService.deleteActivity(lastActivity);
        } else {
            budgetActivityService.deleteActivity(lastActivity);
        }

        return buildBudgetResponse(user, budget);
    }

    @Transactional(readOnly = true)
    public BudgetResponse buildBudgetResponse(User user, Budget budget) {
        List<CategoryGroup> groups = categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(user);
        List<Category> categories = categoryRepository.findByUserOrderBySortOrderAscIdAsc(user);
        List<BudgetAllocation> allocations = budgetAllocationRepository.findByBudget(budget);
        List<Target> targets = targetRepository.findByCategoryIn(categories);

        Map<Long, BudgetAllocation> allocMap = allocations.stream()
                .collect(Collectors.toMap(a -> a.getCategory().getId(), a -> a));

        Map<Long, Target> targetMap = targets.stream()
                .collect(Collectors.toMap(t -> t.getCategory().getId(), t -> t));

        Map<Long, BigDecimal> rolledOverMap = budgetRolloverService.getRolledOverAvailableBalances(user, budget.getYear(), budget.getMonth());

        Map<Long, List<Category>> groupCategoriesMap = categories.stream()
                .collect(Collectors.groupingBy(c -> c.getCategoryGroup().getId()));

        BigDecimal totalAssigned = BigDecimal.ZERO;
        BigDecimal totalActivity = BigDecimal.ZERO;
        BigDecimal totalAvailable = BigDecimal.ZERO;

        int fundedCount = 0;
        int underfundedCount = 0;
        int overfundedCount = 0;
        int targetsMetCount = 0;

        List<GroupBudgetSummary> groupSummaries = new ArrayList<>();
        YearMonth currentYearMonth = YearMonth.of(budget.getYear(), budget.getMonth());

        for (CategoryGroup group : groups) {
            List<Category> groupCats = groupCategoriesMap.getOrDefault(group.getId(), new ArrayList<>());
            List<BudgetAllocationResponse> allocResponses = new ArrayList<>();

            BigDecimal groupAssigned = BigDecimal.ZERO;
            BigDecimal groupActivity = BigDecimal.ZERO;
            BigDecimal groupAvailable = BigDecimal.ZERO;

            for (Category cat : groupCats) {
                Long catId = cat.getId();
                BudgetAllocation alloc = allocMap.get(catId);

                BigDecimal assigned = alloc != null ? alloc.getAssignedAmount() : BigDecimal.ZERO;
                BigDecimal activity = alloc != null ? alloc.getActivityAmount() : BigDecimal.ZERO;

                BigDecimal rolledOver = rolledOverMap.getOrDefault(catId, BigDecimal.ZERO);
                BigDecimal available = rolledOver.add(assigned).subtract(activity);

                if (alloc != null && alloc.getAvailableAmount().compareTo(available) != 0) {
                    alloc.setAvailableAmount(available);
                    budgetAllocationRepository.save(alloc);
                }

                groupAssigned = groupAssigned.add(assigned);
                groupActivity = groupActivity.add(activity);
                groupAvailable = groupAvailable.add(available);

                Target target = targetMap.get(catId);
                TargetResponse targetResp = TargetResponse.buildFromTargetAndAvailable(target, available, currentYearMonth);

                String status = "ZERO";
                if (targetResp != null) {
                    status = targetResp.getStatus();
                    if ("FUNDED".equalsIgnoreCase(status)) fundedCount++;
                    else if ("UNDERFUNDED".equalsIgnoreCase(status)) underfundedCount++;
                    else if ("OVERFUNDED".equalsIgnoreCase(status)) overfundedCount++;
                    else if ("COMPLETED".equalsIgnoreCase(status)) {
                        fundedCount++;
                        targetsMetCount++;
                    }
                } else {
                    if (available.compareTo(BigDecimal.ZERO) > 0) {
                        status = "POSITIVE";
                    } else if (available.compareTo(BigDecimal.ZERO) < 0) {
                        status = "NEGATIVE";
                    }
                }

                allocResponses.add(BudgetAllocationResponse.builder()
                        .id(alloc != null ? alloc.getId() : null)
                        .categoryId(catId)
                        .categoryName(cat.getName())
                        .categoryGroupId(group.getId())
                        .categoryGroupName(group.getName())
                        .icon(cat.getIcon())
                        .color(cat.getColor())
                        .sortOrder(cat.getSortOrder())
                        .assigned(assigned)
                        .activity(activity)
                        .available(available)
                        .target(targetResp)
                        .status(status)
                        .note(cat.getNote())
                        .build());
            }

            totalAssigned = totalAssigned.add(groupAssigned);
            totalActivity = totalActivity.add(groupActivity);
            totalAvailable = totalAvailable.add(groupAvailable);

            groupSummaries.add(GroupBudgetSummary.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .icon(group.getIcon())
                    .color(group.getColor())
                    .sortOrder(group.getSortOrder())
                    .isCollapsed(group.getIsCollapsed())
                    .assigned(groupAssigned)
                    .activity(groupActivity)
                    .available(groupAvailable)
                    .categories(allocResponses)
                    .build());
        }

        LocalDate startDate = currentYearMonth.atDay(1);
        LocalDate endDate = currentYearMonth.atEndOfMonth();
        BigDecimal monthlyIncome = transactionRepository.sumAmountByUserAndTypeAndDateBetween(
                user, TransactionType.INCOME, startDate, endDate
        );
        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;

        BigDecimal startingBalance = budget.getStartingBalance() != null ? budget.getStartingBalance() : BigDecimal.ZERO;
        BigDecimal readyToAssign = startingBalance.add(monthlyIncome).subtract(totalAssigned);

        List<BudgetActivityResponse> recentActivities = budgetActivityService.getRecentActivities(user, budget);

        BudgetHealthSummary healthSummary = BudgetHealthSummary.builder()
                .fundedCount(fundedCount)
                .underfundedCount(underfundedCount)
                .overfundedCount(overfundedCount)
                .targetsMetCount(targetsMetCount)
                .build();

        return BudgetResponse.builder()
                .budgetId(budget.getId())
                .year(budget.getYear())
                .month(budget.getMonth())
                .startingBalance(startingBalance)
                .readyToAssign(readyToAssign)
                .totalAssigned(totalAssigned)
                .totalActivity(totalActivity)
                .totalAvailable(totalAvailable)
                .totalIncome(monthlyIncome)
                .budgetHealth(healthSummary)
                .groups(groupSummaries)
                .recentActivities(recentActivities)
                .build();
    }
}
