package com.finpilot.budget.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoAssignRequest {
    @NotBlank(message = "Auto-assign mode is required")
    private String mode; // FUND_TARGETS, UNDERFUNDED, SAME_AS_LAST_MONTH, LAST_MONTH_SPENDING, REMAINING_TO_SAVINGS
}
