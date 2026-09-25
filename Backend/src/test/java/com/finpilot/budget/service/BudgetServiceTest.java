package com.finpilot.budget.service;

import com.finpilot.budget.dto.AssignMoneyRequest;
import com.finpilot.budget.dto.BudgetResponse;
import com.finpilot.budget.dto.MoveMoneyRequest;
import com.finpilot.budget.entity.Budget;
import com.finpilot.budget.entity.BudgetAllocation;
import com.finpilot.budget.repository.BudgetAllocationRepository;
import com.finpilot.budget.repository.BudgetRepository;
import com.finpilot.category.entity.Category;
import com.finpilot.category.entity.CategoryGroup;
import com.finpilot.category.repository.CategoryGroupRepository;
import com.finpilot.category.repository.CategoryRepository;
import com.finpilot.category.service.CategoryGroupService;
import com.finpilot.target.repository.TargetRepository;
import com.finpilot.user.entity.AuthProvider;
import com.finpilot.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetAllocationRepository budgetAllocationRepository;
    @Mock private CategoryGroupRepository categoryGroupRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TargetRepository targetRepository;
    @Mock private CategoryGroupService categoryGroupService;
    @Mock private BudgetRolloverService budgetRolloverService;
    @Mock private BudgetActivityService budgetActivityService;
    @Mock private com.finpilot.transaction.repository.TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private CategoryGroup testGroup;
    private Category foodCategory;
    private Category groceriesCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Rushikesh")
                .email("rushikesh@example.com")
                .authProvider(AuthProvider.LOCAL)
                .build();

        testBudget = Budget.builder()
                .id(100L)
                .user(testUser)
                .year(2026)
                .month(9)
                .startingBalance(new BigDecimal("48800.00"))
                .build();

        testGroup = CategoryGroup.builder()
                .id(10L)
                .user(testUser)
                .name("Needs")
                .sortOrder(1)
                .isCollapsed(false)
                .build();

        foodCategory = Category.builder()
                .id(101L)
                .user(testUser)
                .categoryGroup(testGroup)
                .name("Food")
                .sortOrder(1)
                .build();

        groceriesCategory = Category.builder()
                .id(102L)
                .user(testUser)
                .categoryGroup(testGroup)
                .name("Groceries")
                .sortOrder(2)
                .build();
    }

    @Test
    @DisplayName("Get or Create Budget returns correctly initialized budget view model")
    void testGetOrCreateBudget() {
        when(budgetRepository.findByUserAndYearAndMonth(testUser, 2026, 9)).thenReturn(Optional.of(testBudget));
        when(categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(testUser)).thenReturn(List.of(testGroup));
        when(categoryRepository.findByUserOrderBySortOrderAscIdAsc(testUser)).thenReturn(List.of(foodCategory, groceriesCategory));
        when(budgetAllocationRepository.findByBudget(testBudget)).thenReturn(Collections.emptyList());
        when(targetRepository.findByCategoryIn(any())).thenReturn(Collections.emptyList());
        when(budgetRolloverService.getRolledOverAvailableBalances(testUser, 2026, 9)).thenReturn(Collections.emptyMap());

        BudgetResponse response = budgetService.getOrCreateBudget(testUser, 2026, 9);

        assertNotNull(response);
        assertEquals(100L, response.getBudgetId());
        assertEquals(2026, response.getYear());
        assertEquals(9, response.getMonth());
        assertEquals(new BigDecimal("48800.00"), response.getStartingBalance());
        assertEquals(new BigDecimal("48800.00"), response.getReadyToAssign());
        assertEquals(1, response.getGroups().size());
        assertEquals(2, response.getGroups().get(0).getCategories().size());
    }

    @Test
    @DisplayName("Set starting balance updates Ready to Assign")
    void testSetStartingBalance() {
        when(budgetRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testBudget));
        when(categoryGroupRepository.findByUserOrderBySortOrderAscIdAsc(testUser)).thenReturn(List.of(testGroup));
        when(categoryRepository.findByUserOrderBySortOrderAscIdAsc(testUser)).thenReturn(List.of(foodCategory));

        BudgetResponse response = budgetService.setStartingBalance(testUser, 100L, new BigDecimal("50000.00"));

        assertNotNull(response);
        assertEquals(new BigDecimal("50000.00"), response.getStartingBalance());
        verify(budgetRepository, times(1)).save(testBudget);
    }
}
