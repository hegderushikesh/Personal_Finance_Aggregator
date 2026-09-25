package com.finpilot.transaction.repository;

import com.finpilot.category.entity.Category;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    Optional<Transaction> findByIdAndUser(Long id, User user);

    List<Transaction> findTop5ByUserOrderByTransactionDateDescCreatedAtDesc(User user);

    List<Transaction> findTop10ByUserAndCategoryOrderByTransactionDateDescCreatedAtDesc(User user, Category category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category = :category AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByUserAndCategoryAndTypeAndDateBetween(
            @Param("user") User user,
            @Param("category") Category category,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByUserAndTypeAndDateBetween(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Page<Transaction> findByUser(User user, Pageable pageable);

    long countByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);

    boolean existsByAccount(com.finpilot.account.entity.Account account);

    Optional<Transaction> findByPlaidConnectionIdAndExternalTransactionId(Long plaidConnectionId, String externalTransactionId);

    Optional<Transaction> findByExternalTransactionIdAndUser(String externalTransactionId, User user);

    List<Transaction> findByPlaidConnectionId(Long plaidConnectionId);

    List<Transaction> findByPlaidConnectionIdAndUser(Long plaidConnectionId, User user);

    boolean existsByPlaidConnectionIdAndExternalTransactionId(Long plaidConnectionId, String externalTransactionId);
}
