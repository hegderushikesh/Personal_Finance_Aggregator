package com.finpilot.budget.repository;

import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetAllocationRepository extends JpaRepository<BudgetAllocation, Long> {

    List<BudgetAllocation> findByBudget(Budget budget);

    Optional<BudgetAllocation> findByBudgetAndCategory(Budget budget, Category category);

    List<BudgetAllocation> findByBudgetIn(Collection<Budget> budgets);

    Optional<BudgetAllocation> findByBudgetAndCategoryId(Budget budget, Long categoryId);
}
