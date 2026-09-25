package com.finpilot.recurring.dto;

import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
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
public class RecurringPaymentResponse {
    private Long id;
    private String merchantName;
    private String normalizedMerchantName;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String categoryIcon;
    private Long accountId;
    private String accountName;
    private BigDecimal averageAmount;
    private BigDecimal lastAmount;
    private RecurringFrequency frequency;
    private Integer intervalDays;
    private LocalDate nextExpectedDate;
    private LocalDate lastTransactionDate;
    private Integer occurrenceCount;
    private BigDecimal amountVariance;
    private RecurringPaymentStatus status;
    private Integer confidence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
