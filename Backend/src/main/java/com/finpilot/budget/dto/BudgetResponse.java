package com.finpilot.budget.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponse {
    private Long budgetId;
    private Integer year;
    private Integer month;
    private BigDecimal startingBalance;
    private BigDecimal readyToAssign;
    private BigDecimal totalAssigned;
    private BigDecimal totalActivity;
    private BigDecimal totalAvailable;
    private BigDecimal totalIncome;
    private BudgetHealthSummary budgetHealth;
    private List<GroupBudgetSummary> groups;
    private List<BudgetActivityResponse> recentActivities;
}
