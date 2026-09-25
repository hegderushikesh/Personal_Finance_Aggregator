package com.finpilot.transaction.dto;

import com.finpilot.transaction.entity.Transaction;
import com.finpilot.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private String accountName;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private String categoryColor;
    private String merchantName;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private TransactionType type;
    private String description;
    private com.finpilot.transaction.entity.TransactionSource source;
    private String externalTransactionId;
    private Boolean pending;
    private Long plaidConnectionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .accountId(tx.getAccount() != null ? tx.getAccount().getId() : null)
                .accountName(tx.getAccount() != null ? tx.getAccount().getName() : null)
                .categoryId(tx.getCategory() != null ? tx.getCategory().getId() : null)
                .categoryName(tx.getCategory() != null ? tx.getCategory().getName() : null)
                .categoryIcon(tx.getCategory() != null ? tx.getCategory().getIcon() : null)
                .categoryColor(tx.getCategory() != null ? tx.getCategory().getColor() : null)
                .merchantName(tx.getMerchantName())
                .amount(tx.getAmount())
                .transactionDate(tx.getTransactionDate())
                .type(tx.getType())
                .description(tx.getDescription())
                .source(tx.getSource() != null ? tx.getSource() : com.finpilot.transaction.entity.TransactionSource.MANUAL)
                .externalTransactionId(tx.getExternalTransactionId())
                .pending(Boolean.TRUE.equals(tx.getPending()))
                .plaidConnectionId(tx.getPlaidConnectionId())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
