package com.finpilot.budget.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoAssignPreviewItem {
    private Long categoryId;
    private String categoryName;
    private BigDecimal currentAssigned;
    private BigDecimal additionalAmount;
    private BigDecimal newAssigned;
}
