package com.finpilot.plaid.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeTokenRequest {

    @NotBlank
    private String publicToken;

    private String institutionId;
    private String institutionName;
}
