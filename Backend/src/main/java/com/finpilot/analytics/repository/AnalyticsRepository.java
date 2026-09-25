package com.finpilot.analytics.repository;

import com.finpilot.analytics.repository.projection.CategorySpendingProjection;
import com.finpilot.analytics.repository.projection.IncomeExpenseProjection;
import com.finpilot.analytics.repository.projection.SpendingProjection;
import com.finpilot.analytics.repository.projection.SpendingTrendProjection;
import com.finpilot.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT " +
            "COALESCE(c.name, 'Uncategorized') AS category, " +
            "COALESCE(SUM(t.amount), 0) AS amount " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "WHERE t.user_id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY COALESCE(c.name, 'Uncategorized') " +
            "ORDER BY amount DESC", nativeQuery = true)
    List<SpendingProjection> findSpendingByCategory(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT " +
            "TO_CHAR(t.transaction_date, 'YYYY-MM') AS month, " +
            "COALESCE(SUM(t.amount), 0) AS amount " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY TO_CHAR(t.transaction_date, 'YYYY-MM') " +
            "ORDER BY month ASC", nativeQuery = true)
    List<SpendingTrendProjection> findSpendingTrends(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT " +
            "TO_CHAR(t.transaction_date, 'YYYY-MM') AS month, " +
            "COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) AS income, " +
            "COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) AS expense " +
            "FROM transactions t " +
            "WHERE t.user_id = :userId " +
            "AND t.type IN ('INCOME', 'EXPENSE') " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY TO_CHAR(t.transaction_date, 'YYYY-MM') " +
            "ORDER BY month ASC", nativeQuery = true)
    List<IncomeExpenseProjection> findMonthlyIncomeAndExpense(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = "SELECT " +
            "t.category_id AS categoryId, " +
            "COALESCE(c.name, 'Uncategorized') AS category, " +
            "COALESCE(cg.name, 'Uncategorized') AS categoryGroup, " +
            "COALESCE(SUM(t.amount), 0) AS amount, " +
            "COUNT(t.id) AS transactionCount " +
            "FROM transactions t " +
            "LEFT JOIN categories c ON t.category_id = c.id " +
            "LEFT JOIN category_groups cg ON c.category_group_id = cg.id " +
            "WHERE t.user_id = :userId " +
            "AND t.type = 'EXPENSE' " +
            "AND t.transaction_date BETWEEN :startDate AND :endDate " +
            "GROUP BY t.category_id, c.name, cg.name " +
            "ORDER BY amount DESC", nativeQuery = true)
    List<CategorySpendingProjection> findCategorySpending(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
