package com.sportlink.moderation.model;

import com.sportlink.moderation.model.enums.AccountSyncStatus;
import com.sportlink.moderation.model.enums.SanctionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_sanctions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSanction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sanction_id")
    private UUID sanctionId;

    @Column(name = "case_id", nullable = false)
    private UUID caseId;

    @Column(name = "target_user_id", nullable = false)
    private UUID targetUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sanction_type", nullable = false)
    private SanctionType sanctionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_sync_status", nullable = false)
    private AccountSyncStatus accountSyncStatus;

    @Column(name = "sync_attempts", nullable = false)
    private int syncAttempts;

    @Column(name = "last_sync_error")
    private String lastSyncError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.accountSyncStatus == null) {
            this.accountSyncStatus = AccountSyncStatus.SYNCED;
        }
    }
}
