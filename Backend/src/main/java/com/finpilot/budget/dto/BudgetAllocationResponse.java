package com.finpilot.budget.dto;

import com.finpilot.target.dto.TargetResponse;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAllocationResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long categoryGroupId;
    private String categoryGroupName;
    private String icon;
    private String color;
    private Integer sortOrder;
    private BigDecimal assigned;
    private BigDecimal activity;
    private BigDecimal available;
    private TargetResponse target;
    private String status; // FUNDED, UNDERFUNDED, OVERFUNDED, ZERO, NEGATIVE
    private String note;
}
