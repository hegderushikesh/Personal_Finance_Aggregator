package com.finpilot.plaid.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncResponse {
    private int added;
    private int modified;
    private int removed;
    private int accountsUpdated;
    private LocalDateTime lastSyncedAt;
}
