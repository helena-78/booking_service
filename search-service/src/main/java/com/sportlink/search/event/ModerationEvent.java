package com.sportlink.search.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationEvent {

    /**
     * One of: USER_RESTRICTED, USER_STATUS_UPDATED. Unknown future values
     * are ignored by the consumer.
     */
    private String eventType;

    /** Subject of the moderation action. */
    private String userId;

    /** The moderation case that produced the event (audit only). */
    private String caseId;

    /** Sanction type, if applicable: SUSPENSION / BAN / WARNING. */
    private String sanctionType;

    /**
     * New user status from Account Management's perspective:
     * ACTIVE / SUSPENDED / BANNED.
     */
    private String userStatus;

    /** Optional free-text reason (audit/debug only - not indexed verbatim). */
    private String reason;

    /** When the event happened on the producer side. */
    private LocalDateTime occurredAt;
}
