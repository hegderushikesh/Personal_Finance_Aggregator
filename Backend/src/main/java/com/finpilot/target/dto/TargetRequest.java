package com.finpilot.target.dto;

import com.finpilot.target.entity.TargetFrequency;
import com.finpilot.target.entity.TargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetRequest {
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Target type is required")
    private TargetType type;

    @NotNull(message = "Target amount is required")
    @Positive(message = "Target amount must be positive")
    private BigDecimal amount;

    private LocalDate targetDate;

    private TargetFrequency frequency;

    private BigDecimal monthlyAmount;
}
