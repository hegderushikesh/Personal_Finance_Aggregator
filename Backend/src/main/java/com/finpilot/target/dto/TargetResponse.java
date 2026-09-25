package com.finpilot.target.dto;

import com.finpilot.target.entity.Target;
import com.finpilot.target.entity.TargetFrequency;
import com.finpilot.target.entity.TargetType;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetResponse {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private TargetType type;
    private BigDecimal amount;
    private LocalDate targetDate;
    private TargetFrequency frequency;
    private BigDecimal monthlyAmount;
    private Boolean snoozed;
    private Double progressPercentage;
    private BigDecimal remainingAmount;
    private BigDecimal monthlyRequired;
    private String status; // FUNDED, UNDERFUNDED, OVERFUNDED, COMPLETED, SNOOZED

    public static TargetResponse buildFromTargetAndAvailable(Target target, BigDecimal currentAvailable, YearMonth currentYearMonth) {
        if (target == null) return null;

        BigDecimal targetAmount = target.getAmount() != null ? target.getAmount() : BigDecimal.ZERO;
        BigDecimal available = currentAvailable != null ? currentAvailable : BigDecimal.ZERO;

        BigDecimal remaining = targetAmount.subtract(available);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        double progressPercentage = 0.0;
        if (targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = available.multiply(BigDecimal.valueOf(100))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP)
                    .doubleValue();
            if (progressPercentage > 100.0) {
                progressPercentage = 100.0;
            }
        }

        BigDecimal monthlyRequired = target.getMonthlyAmount();
        if (target.getType() == TargetType.TARGET_BY_DATE && target.getTargetDate() != null) {
            YearMonth targetYearMonth = YearMonth.from(target.getTargetDate());
            long monthsBetween = ChronoUnit.MONTHS.between(currentYearMonth, targetYearMonth) + 1;
            if (monthsBetween <= 0) {
                monthlyRequired = remaining;
            } else {
                monthlyRequired = remaining.divide(BigDecimal.valueOf(monthsBetween), 2, RoundingMode.CEILING);
            }
        } else if (target.getType() == TargetType.MONTHLY_AMOUNT) {
            monthlyRequired = targetAmount;
        } else if (monthlyRequired == null) {
            monthlyRequired = remaining;
        }

        String status = "UNDERFUNDED";
        if (Boolean.TRUE.equals(target.getSnoozed())) {
            status = "SNOOZED";
        } else if (available.compareTo(targetAmount) >= 0 && targetAmount.compareTo(BigDecimal.ZERO) > 0) {
            status = "COMPLETED";
        } else if (available.compareTo(monthlyRequired) >= 0) {
            status = "FUNDED";
        } else if (available.compareTo(targetAmount) > 0) {
            status = "OVERFUNDED";
        }

        return TargetResponse.builder()
                .id(target.getId())
                .categoryId(target.getCategory().getId())
                .categoryName(target.getCategory().getName())
                .type(target.getType())
                .amount(targetAmount)
                .targetDate(target.getTargetDate())
                .frequency(target.getFrequency())
                .monthlyAmount(monthlyRequired)
                .snoozed(target.getSnoozed())
                .progressPercentage(progressPercentage)
                .remainingAmount(remaining)
                .monthlyRequired(monthlyRequired)
                .status(status)
                .build();
    }
}
