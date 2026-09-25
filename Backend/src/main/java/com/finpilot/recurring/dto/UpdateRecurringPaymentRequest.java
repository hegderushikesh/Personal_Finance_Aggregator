package com.finpilot.recurring.dto;

import com.finpilot.recurring.entity.RecurringFrequency;
import com.finpilot.recurring.entity.RecurringPaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecurringPaymentRequest {
    private String merchantName;
    private Long categoryId;
    private RecurringFrequency frequency;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate nextExpectedDate;
    private RecurringPaymentStatus status;
}
