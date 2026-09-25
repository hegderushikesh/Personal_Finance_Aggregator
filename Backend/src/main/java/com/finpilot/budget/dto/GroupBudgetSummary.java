package com.finpilot.budget.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupBudgetSummary {
    private Long id;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Boolean isCollapsed;
    private BigDecimal assigned;
    private BigDecimal activity;
    private BigDecimal available;
    private List<BudgetAllocationResponse> categories;
}
