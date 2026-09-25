package com.finpilot.recurring.entity;

import com.finpilot.account.entity.Account;
import com.finpilot.category.entity.Category;
import com.finpilot.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "recurring_payments",
    indexes = {
        @Index(name = "idx_recurring_user_id", columnList = "user_id"),
        @Index(name = "idx_recurring_norm_merchant", columnList = "user_id, normalized_merchant_name"),
        @Index(name = "idx_recurring_status", columnList = "status"),
        @Index(name = "idx_recurring_next_date", columnList = "next_expected_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "merchant_name", nullable = false, length = 150)
    private String merchantName;

    @NotBlank
    @Column(name = "normalized_merchant_name", nullable = false, length = 150)
    private String normalizedMerchantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @NotNull
    @Column(name = "average_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageAmount;

    @NotNull
    @Column(name = "last_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal lastAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RecurringFrequency frequency;

    @NotNull
    @Column(name = "interval_days", nullable = false)
    private Integer intervalDays;

    @NotNull
    @Column(name = "next_expected_date", nullable = false)
    private LocalDate nextExpectedDate;

    @NotNull
    @Column(name = "last_transaction_date", nullable = false)
    private LocalDate lastTransactionDate;

    @NotNull
    @Column(name = "occurrence_count", nullable = false)
    private Integer occurrenceCount;

    @NotNull
    @Column(name = "amount_variance", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal amountVariance = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RecurringPaymentStatus status = RecurringPaymentStatus.ACTIVE;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Integer confidence = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = RecurringPaymentStatus.ACTIVE;
        }
        if (this.confidence == null) {
            this.confidence = 0;
        }
        if (this.amountVariance == null) {
            this.amountVariance = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
