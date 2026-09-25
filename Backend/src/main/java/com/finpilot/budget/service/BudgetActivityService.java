package com.finpilot.budget.service;

import com.finpilot.budget.dto.BudgetActivityResponse;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetActivity;
import com.finpilot.budget.entity.BudgetActivityType;
import com.finpilot.budget.repository.BudgetActivityRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetActivityService {

    private final BudgetActivityRepository budgetActivityRepository;

    @Transactional
    public BudgetActivity logActivity(User user, Budget budget, BudgetActivityType type,
                                      Category source, Category dest, BigDecimal amount, String description) {
        BudgetActivity activity = BudgetActivity.builder()
                .user(user)
                .budget(budget)
                .type(type)
                .sourceCategory(source)
                .destinationCategory(dest)
                .amount(amount)
                .description(description)
                .build();
        return budgetActivityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<BudgetActivityResponse> getRecentActivities(User user, Budget budget) {
        return budgetActivityRepository.findByUserAndBudgetOrderByCreatedAtDesc(user, budget).stream()
                .map(BudgetActivityResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BudgetActivity> getLastActivity(User user, Budget budget) {
        return budgetActivityRepository.findFirstByUserAndBudgetOrderByCreatedAtDesc(user, budget);
    }

    @Transactional
    public void deleteActivity(BudgetActivity activity) {
        budgetActivityRepository.delete(activity);
    }
}
