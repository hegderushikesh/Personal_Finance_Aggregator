package com.finpilot.plaid.dto;

import com.finpilot.plaid.entity.PlaidConnection;
import com.finpilot.plaid.entity.PlaidConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaidConnectionResponse {
    private Long id;
    private String institutionName;
    private String institutionId;
    private PlaidConnectionStatus status;
    private int accountCount;
    private LocalDateTime lastSyncedAt;
    private LocalDateTime createdAt;

    public static PlaidConnectionResponse from(PlaidConnection conn, int accountCount) {
        return PlaidConnectionResponse.builder()
                .id(conn.getId())
                .institutionName(conn.getInstitutionName() != null ? conn.getInstitutionName() : "Bank Account")
                .institutionId(conn.getInstitutionId())
                .status(conn.getStatus())
                .accountCount(accountCount)
                .lastSyncedAt(conn.getLastSyncedAt())
                .createdAt(conn.getCreatedAt())
                .build();
    }
}
