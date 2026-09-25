package com.finpilot.account.dto;

import com.finpilot.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Account type is required")
    private AccountType type;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Initial balance must be greater than or equal to 0")
    private BigDecimal balance;

    @Size(max = 100, message = "Institution name must not exceed 100 characters")
    private String institutionName;

    @Pattern(regexp = "^[0-9]{4}$", message = "Account number last 4 digits must be exactly 4 numbers")
    private String accountNumberLast4;

    @Builder.Default
    private String currency = "INR";
}
