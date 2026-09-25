package com.finpilot.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoveMoneyRequest {
    @NotNull(message = "Source category ID is required")
    private Long fromCategoryId;

    @NotNull(message = "Destination category ID is required")
    private Long toCategoryId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}
