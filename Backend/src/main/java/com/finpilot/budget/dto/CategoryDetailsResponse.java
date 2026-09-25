package com.finpilot.budget.dto;

import com.finpilot.target.dto.TargetResponse;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDetailsResponse {
    private Long categoryId;
    private String categoryName;
    private String categoryGroupName;
    private String icon;
    private String color;
    private BigDecimal cashLeftFromLastMonth;
    private BigDecimal assignedThisMonth;
    private BigDecimal activity;
    private BigDecimal available;
    private TargetResponse target;
    private String note;
    private java.util.List<com.finpilot.transaction.dto.TransactionResponse> recentTransactions;
}
