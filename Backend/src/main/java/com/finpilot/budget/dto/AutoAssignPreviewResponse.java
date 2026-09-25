package com.finpilot.budget.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoAssignPreviewResponse {
    private String mode;
    private BigDecimal totalRequired;
    private BigDecimal readyToAssignAfter;
    private List<AutoAssignPreviewItem> items;
}
