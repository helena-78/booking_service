package com.sportlink.scheduling.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirror of Activity Management's ActivityCancelledEvent.
 *
 * Each service owns its copy of the event DTO. As long as the JSON
 * structure matches, Spring Kafka will deserialize messages from the producer into this class.Spring Kafka will deserialize messages from the producer into this class.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCancelledEvent {

    private UUID activityId;
    private UUID preferredTimeSlotId;
    private LocalDateTime cancelledAt;
}