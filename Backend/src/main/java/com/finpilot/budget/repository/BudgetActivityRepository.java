package com.finpilot.budget.repository;

import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetActivity;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetActivityRepository extends JpaRepository<BudgetActivity, Long> {

    List<BudgetActivity> findByUserAndBudgetOrderByCreatedAtDesc(User user, Budget budget);

    Optional<BudgetActivity> findFirstByUserAndBudgetOrderByCreatedAtDesc(User user, Budget budget);
}
