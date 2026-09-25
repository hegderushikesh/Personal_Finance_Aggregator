package com.finpilot.plaid.entity;

import com.finpilot.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "plaid_connections",
    indexes = {
        @Index(name = "idx_plaid_user_id", columnList = "user_id"),
        @Index(name = "idx_plaid_item_id", columnList = "item_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaidConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(name = "item_id", nullable = false, unique = true, length = 100)
    private String itemId;

    @NotBlank
    @Column(name = "access_token", nullable = false, length = 255)
    private String accessToken;

    @Column(name = "institution_id", length = 100)
    private String institutionId;

    @Column(name = "institution_name", length = 150)
    private String institutionName;

    @Column(name = "sync_cursor", columnDefinition = "TEXT")
    private String cursor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private PlaidConnectionStatus status = PlaidConnectionStatus.ACTIVE;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PlaidConnectionStatus.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
