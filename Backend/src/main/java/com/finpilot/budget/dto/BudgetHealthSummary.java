package com.finpilot.budget.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetHealthSummary {
    private int fundedCount;
    private int underfundedCount;
    private int overfundedCount;
    private int targetsMetCount;
}
