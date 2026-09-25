package com.finpilot.budget.dto;

import com.finpilot.budget.entity.BudgetActivity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetActivityResponse {
    private Long id;
    private String type;
    private Long sourceCategoryId;
    private String sourceCategoryName;
    private Long destinationCategoryId;
    private String destinationCategoryName;
    private BigDecimal amount;
    private String description;
    private LocalDateTime createdAt;

    public static BudgetActivityResponse from(BudgetActivity activity) {
        return BudgetActivityResponse.builder()
                .id(activity.getId())
                .type(activity.getType().name())
                .sourceCategoryId(activity.getSourceCategory() != null ? activity.getSourceCategory().getId() : null)
                .sourceCategoryName(activity.getSourceCategory() != null ? activity.getSourceCategory().getName() : null)
                .destinationCategoryId(activity.getDestinationCategory() != null ? activity.getDestinationCategory().getId() : null)
                .destinationCategoryName(activity.getDestinationCategory() != null ? activity.getDestinationCategory().getName() : null)
                .amount(activity.getAmount())
                .description(activity.getDescription())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
