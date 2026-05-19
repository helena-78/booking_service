package com.sportlink.moderation.service;

import com.sportlink.moderation.client.AccountManagementClient;
import com.sportlink.moderation.dto.request.ApplySanctionRequest;
import com.sportlink.moderation.dto.response.AccountSanctionResponse;
import com.sportlink.moderation.event.ModerationEvent;
import com.sportlink.moderation.event.ModerationEventType;
import com.sportlink.moderation.exception.AccountSyncException;
import com.sportlink.moderation.messaging.ModerationEventPublisher;
import com.sportlink.moderation.model.AccountSanction;
import com.sportlink.moderation.model.enums.AccountSyncStatus;
import com.sportlink.moderation.model.enums.SanctionType;
import com.sportlink.moderation.repository.AccountSanctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SanctionService {

    private final AccountSanctionRepository sanctionRepository;
    private final AccountManagementClient accountManagementClient;
    private final ModerationEventPublisher eventPublisher;

    @Transactional
    public AccountSanctionResponse applySanction(ApplySanctionRequest request) {
        AccountSanction sanction = AccountSanction.builder()
                .caseId(request.getCaseId())
                .targetUserId(request.getTargetUserId())
                .sanctionType(request.getSanctionType())
                .accountSyncStatus(AccountSyncStatus.SYNCED)
                .syncAttempts(0)
                .build();

        String targetStatus = mapToAccountStatus(request.getSanctionType());

        try {
            sanction.setSyncAttempts(1);
            accountManagementClient.updateUserStatus(request.getTargetUserId(), targetStatus);
            sanction.setAccountSyncStatus(AccountSyncStatus.SYNCED);
        } catch (AccountSyncException ex) {
            log.warn("Account Management sync failed for sanction on user {}: {}",
                    request.getTargetUserId(), ex.getMessage());
            sanction.setAccountSyncStatus(AccountSyncStatus.PENDING_RETRY);
            sanction.setLastSyncError(ex.getMessage());
        }

        AccountSanction saved = sanctionRepository.save(sanction);
        log.info("Sanction {} created for user {} (sync status: {})",
                saved.getSanctionId(), saved.getTargetUserId(), saved.getAccountSyncStatus());

        eventPublisher.publishUserRestricted(ModerationEvent.builder()
                .eventType(ModerationEventType.USER_RESTRICTED)
                .sanctionId(saved.getSanctionId())
                .caseId(saved.getCaseId())
                .targetUserId(saved.getTargetUserId())
                .sanctionType(saved.getSanctionType() != null ? saved.getSanctionType().name() : null)
                .occurredAt(LocalDateTime.now())
                .build());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountSanctionResponse> getSanctionHistory(UUID userId) {
        return sanctionRepository.findByTargetUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private String mapToAccountStatus(SanctionType type) {
        return type == SanctionType.BAN ? "BANNED" : "SUSPENDED";
    }

    private AccountSanctionResponse toResponse(AccountSanction entity) {
        return AccountSanctionResponse.builder()
                .sanctionId(entity.getSanctionId())
                .caseId(entity.getCaseId())
                .targetUserId(entity.getTargetUserId())
                .sanctionType(entity.getSanctionType())
                .accountSyncStatus(entity.getAccountSyncStatus())
                .syncAttempts(entity.getSyncAttempts())
                .lastSyncError(entity.getLastSyncError())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
