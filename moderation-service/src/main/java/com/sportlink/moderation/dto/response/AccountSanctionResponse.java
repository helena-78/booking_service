package com.sportlink.moderation.dto.response;

import com.sportlink.moderation.model.enums.AccountSyncStatus;
import com.sportlink.moderation.model.enums.SanctionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSanctionResponse {

    private UUID sanctionId;
    private UUID caseId;
    private UUID targetUserId;
    private SanctionType sanctionType;
    private AccountSyncStatus accountSyncStatus;
    private int syncAttempts;
    private String lastSyncError;
    private LocalDateTime createdAt;
}
