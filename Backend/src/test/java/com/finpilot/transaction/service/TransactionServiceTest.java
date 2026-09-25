package com.finpilot.transaction.service;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountType;
import com.finpilot.account.repository.AccountRepository;
import com.finpilot.budget.service.BudgetTransactionService;
import com.finpilot.category.entity.Category;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.transaction.dto.CreateTransactionRequest;
import com.finpilot.transaction.dto.TransactionResponse;
import com.finpilot.transaction.dto.TransactionSummaryResponse;
import com.finpilot.transaction.dto.UpdateTransactionRequest;
import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import com.finpilot.transaction.repository.TransactionRepository;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private BudgetTransactionService budgetTransactionService;

    @InjectMocks
    private TransactionService transactionService;

    private User user1;
    private User user2;
    private Account account1;
    private Category foodCategory;
    private Category diningCategory;

    @BeforeEach
    void setUp() {
        user1 = User.builder().id(1L).email("user1@example.com").name("User One").authProvider(AuthProvider.LOCAL).build();
        user2 = User.builder().id(2L).email("user2@example.com").name("User Two").authProvider(AuthProvider.LOCAL).build();

        account1 = Account.builder().id(10L).user(user1).name("HDFC Bank").type(AccountType.CHECKING).balance(new BigDecimal("10000.00")).build();

        CategoryGroup group = CategoryGroup.builder().id(100L).user(user1).name("Essentials").build();
        foodCategory = Category.builder().id(201L).user(user1).categoryGroup(group).name("Food").build();
        diningCategory = Category.builder().id(202L).user(user1).categoryGroup(group).name("Dining Out").build();
    }

    @Test
    @DisplayName("Create Expense Transaction — reduces account balance and recalculates budget")
    void testCreateExpenseTransaction() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(account1.getId())
                .categoryId(foodCategory.getId())
                .merchantName("Swiggy")
                .amount(new BigDecimal("450.00"))
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .description("Dinner")
                .build();

        when(accountRepository.findByIdAndUser(10L, user1)).thenReturn(Optional.of(account1));
        when(categoryRepository.findByIdAndUser(201L, user1)).thenReturn(Optional.of(foodCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1001L);
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(user1, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("450.00"), response.getAmount());
        assertEquals("Swiggy", response.getMerchantName());
        assertEquals(new BigDecimal("9550.00"), account1.getBalance());

        verify(budgetTransactionService, times(1)).recalculateCategoryActivity(user1, foodCategory, 2026, 9);
    }

    @Test
    @DisplayName("Create Income Transaction — increases account balance, does not require category")
    void testCreateIncomeTransaction() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(account1.getId())
                .merchantName("Employer")
                .amount(new BigDecimal("50000.00"))
                .transactionDate(LocalDate.of(2026, 9, 1))
                .type(TransactionType.INCOME)
                .description("Monthly Salary")
                .build();

        when(accountRepository.findByIdAndUser(10L, user1)).thenReturn(Optional.of(account1));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction tx = invocation.getArgument(0);
            tx.setId(1002L);
            return tx;
        });

        TransactionResponse response = transactionService.createTransaction(user1, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("60000.00"), account1.getBalance());
        verify(budgetTransactionService, never()).recalculateCategoryActivity(any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("User cannot create transaction with another user's account")
    void testCreateTransactionWithUnauthorizedAccount() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(999L)
                .categoryId(foodCategory.getId())
                .merchantName("Swiggy")
                .amount(new BigDecimal("450.00"))
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .build();

        when(accountRepository.findByIdAndUser(999L, user1)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> transactionService.createTransaction(user1, request));
    }

    @Test
    @DisplayName("User cannot access another user's transaction")
    void testAccessAnotherUsersTransaction() {
        when(transactionRepository.findByIdAndUser(1001L, user2)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> transactionService.getTransactionById(user2, 1001L));
    }

    @Test
    @DisplayName("Update Transaction — updates amount and category correctly")
    void testUpdateTransactionAmountAndCategory() {
        Transaction existing = Transaction.builder()
                .id(501L)
                .user(user1)
                .account(account1)
                .category(foodCategory)
                .amount(new BigDecimal("450.00"))
                .merchantName("Swiggy")
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .build();
        account1.setBalance(new BigDecimal("9550.00"));

        UpdateTransactionRequest updateReq = UpdateTransactionRequest.builder()
                .accountId(account1.getId())
                .categoryId(diningCategory.getId())
                .merchantName("Swiggy")
                .amount(new BigDecimal("600.00"))
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .build();

        when(transactionRepository.findByIdAndUser(501L, user1)).thenReturn(Optional.of(existing));
        when(accountRepository.findByIdAndUser(10L, user1)).thenReturn(Optional.of(account1));
        when(categoryRepository.findByIdAndUser(202L, user1)).thenReturn(Optional.of(diningCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        TransactionResponse response = transactionService.updateTransaction(user1, 501L, updateReq);

        assertNotNull(response);
        assertEquals(new BigDecimal("600.00"), response.getAmount());
        assertEquals("Dining Out", response.getCategoryName());
        // 9550 + 450 (revert old) - 600 (apply new) = 9400
        assertEquals(new BigDecimal("9400.00"), account1.getBalance());

        // Both previous category and new category must be recalculated
        verify(budgetTransactionService, times(1)).recalculateCategoryActivity(user1, foodCategory, 2026, 9);
        verify(budgetTransactionService, times(1)).recalculateCategoryActivity(user1, diningCategory, 2026, 9);
    }

    @Test
    @DisplayName("Delete Transaction — reverses account balance and budget activity")
    void testDeleteTransaction() {
        Transaction existing = Transaction.builder()
                .id(501L)
                .user(user1)
                .account(account1)
                .category(foodCategory)
                .amount(new BigDecimal("450.00"))
                .merchantName("Swiggy")
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .build();
        account1.setBalance(new BigDecimal("9550.00"));

        when(transactionRepository.findByIdAndUser(501L, user1)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(user1, 501L);

        // Account balance reverted from 9550 back to 10000
        assertEquals(new BigDecimal("10000.00"), account1.getBalance());
        verify(transactionRepository, times(1)).delete(existing);
        verify(budgetTransactionService, times(1)).recalculateCategoryActivity(user1, foodCategory, 2026, 9);
    }

    @Test
    @DisplayName("Transaction Summary calculation")
    void testTransactionSummary() {
        LocalDate start = LocalDate.of(2026, 9, 1);
        LocalDate end = LocalDate.of(2026, 9, 30);

        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(user1, TransactionType.INCOME, start, end))
                .thenReturn(new BigDecimal("50000.00"));
        when(transactionRepository.sumAmountByUserAndTypeAndDateBetween(user1, TransactionType.EXPENSE, start, end))
                .thenReturn(new BigDecimal("15000.00"));

        TransactionSummaryResponse summary = transactionService.getTransactionSummary(user1, 9, 2026, null, null);

        assertNotNull(summary);
        assertEquals(new BigDecimal("50000.00"), summary.getTotalIncome());
        assertEquals(new BigDecimal("15000.00"), summary.getTotalExpenses());
        assertEquals(new BigDecimal("35000.00"), summary.getNetCashFlow());
    }

    @Test
    @DisplayName("Zero or negative amount is rejected")
    void testZeroOrNegativeAmount() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(account1.getId())
                .categoryId(foodCategory.getId())
                .merchantName("Swiggy")
                .amount(BigDecimal.ZERO)
                .transactionDate(LocalDate.of(2026, 9, 10))
                .type(TransactionType.EXPENSE)
                .build();

        assertThrows(ResponseStatusException.class, () -> transactionService.createTransaction(user1, request));
    }
}
