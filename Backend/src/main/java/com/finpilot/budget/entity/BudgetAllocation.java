package com.finpilot.budget.entity;

import com.finpilot.category.entity.Category;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "budget_allocations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"budget_id", "category_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BudgetAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "budget_id", nullable = false)
    private Budget budget;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "assigned_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal assignedAmount = BigDecimal.ZERO;

    @Column(name = "activity_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal activityAmount = BigDecimal.ZERO;

    @Column(name = "available_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal availableAmount = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
