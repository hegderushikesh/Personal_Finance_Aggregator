package com.finpilot.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeTokenResponse {
    private boolean success;
    private String itemId;
    private String institutionName;
    private Long connectionId;
    private int accountsCount;
    private int transactionsCount;
}
