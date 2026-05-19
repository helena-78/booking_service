package com.sportlink.activitymanagement.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain event published when an activity is cancelled.
 *
 * Carries everything downstream consumers need to react without calling back into Activity Management
 * Currently consumed by Scheduling Service to release the reserved slot.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCancelledEvent {

    /** The activity that was cancelled. */
    private UUID activityId;

    /** The slot that was reserved for this activity (may be null if none was reserved). */
    private UUID preferredTimeSlotId;

    /** When the cancellation happened. */
    private LocalDateTime cancelledAt;
}