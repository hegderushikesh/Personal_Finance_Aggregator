package com.finpilot.account.dto;

import com.finpilot.account.entity.Account;
import com.finpilot.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private Long id;
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private String institutionName;
    private String accountNumberLast4;
    private String currency;
    private Boolean isActive;
    private com.finpilot.account.entity.AccountSource source;
    private String externalAccountId;
    private Long plaidConnectionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse from(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .name(account.getName())
                .type(account.getType())
                .balance(account.getBalance())
                .institutionName(account.getInstitutionName())
                .accountNumberLast4(account.getAccountNumberLast4())
                .currency(account.getCurrency())
                .isActive(account.getIsActive())
                .source(account.getSource() != null ? account.getSource() : com.finpilot.account.entity.AccountSource.MANUAL)
                .externalAccountId(account.getExternalAccountId())
                .plaidConnectionId(account.getPlaidConnectionId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
