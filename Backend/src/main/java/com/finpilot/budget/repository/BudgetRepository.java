package com.finpilot.budget.repository;

import com.finpilot.budget.entity.Budget;
import com.finpilot.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByUserAndYearAndMonth(User user, Integer year, Integer month);

    Optional<Budget> findByIdAndUser(Long id, User user);

    List<Budget> findByUserOrderByYearAscMonthAsc(User user);
}
