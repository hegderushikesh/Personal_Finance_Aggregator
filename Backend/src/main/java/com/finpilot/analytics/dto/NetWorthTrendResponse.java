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
public class NetWorthTrendResponse {
    private String month;
    private BigDecimal assets;
    private BigDecimal liabilities;
    private BigDecimal netWorth;
}
