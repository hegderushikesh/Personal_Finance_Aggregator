package com.finpilot.budget.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartingBalanceRequest {
    @NotNull(message = "Starting balance is required")
    private BigDecimal startingBalance;
}
