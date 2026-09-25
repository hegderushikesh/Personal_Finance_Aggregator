package com.finpilot.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashFlowResponse {
    private String month;
    private BigDecimal income;
    private BigDecimal expense;
    private BigDecimal netCashFlow;
}
