package com.sportlink.moderation.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Single envelope for everything published to the 'moderation-events' topic.
 * Subtype lives in {@link #eventType}; fields irrelevant to a given subtype
 * stay null and are stripped from the JSON payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModerationEvent {

    private ModerationEventType eventType;
    private UUID sanctionId;
    private UUID caseId;
    private UUID targetUserId;

    /** WARNING / SUSPENSION / BAN. String so consumers don't need our enum. */
    private String sanctionType;

    private LocalDateTime occurredAt;
}
