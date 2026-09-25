package com.finpilot.account.dto;

import com.finpilot.account.entity.AccountType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String name;

    private AccountType type;

    @Size(max = 100, message = "Institution name must not exceed 100 characters")
    private String institutionName;

    @Pattern(regexp = "^[0-9]{4}$", message = "Account number last 4 digits must be exactly 4 numbers")
    private String accountNumberLast4;

    private String currency;

    private Boolean isActive;
}
