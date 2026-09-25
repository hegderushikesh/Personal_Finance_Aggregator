package com.finpilot.transaction.entity;

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
    name = "transactions",
    indexes = {
        @Index(name = "idx_tx_user_date", columnList = "user_id, transaction_date"),
        @Index(name = "idx_tx_user_cat", columnList = "user_id, category_id"),
        @Index(name = "idx_tx_account", columnList = "account_id"),
        @Index(name = "idx_tx_user_type_date", columnList = "user_id, type, transaction_date"),
        @Index(name = "idx_tx_plaid_ext_id", columnList = "plaid_connection_id, external_transaction_id"),
        @Index(name = "idx_tx_ext_id", columnList = "external_transaction_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @NotBlank
    @Column(name = "merchant_name", nullable = false, length = 150)
    private String merchantName;

    @NotNull
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20, columnDefinition = "varchar(20) default 'MANUAL'")
    @Builder.Default
    private TransactionSource source = TransactionSource.MANUAL;

    @Column(name = "external_transaction_id", length = 100)
    private String externalTransactionId;

    @Column(name = "is_pending", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean pending = false;

    @Column(name = "plaid_connection_id")
    private Long plaidConnectionId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.source == null) {
            this.source = TransactionSource.MANUAL;
        }
        if (this.pending == null) {
            this.pending = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
