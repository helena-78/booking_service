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
public class ActivityEvent {

    /**
     * One of: ACTIVITY_CREATED, ACTIVITY_CANCELLED, PARTICIPANT_JOINED,
     * PARTICIPANT_LEFT. Unknown future values are ignored by the consumer.
     */
    private String eventType;

    /** Activity that produced the event. */
    private String activityId;

    /** Organizer (creator) of the activity. */
    private String organizerId;

    /** Activity title (only set on CREATED so the index can store displayName). */
    private String activityTitle;

    /** Sport type (e.g. BASKETBALL). */
    private String sportType;

    /** Free-text location (e.g. "Tartu"). */
    private String location;

    /** Activity status (OPEN / CLOSED / CANCELLED / FULL). */
    private String activityStatus;

    /** Maximum number of participants the activity accepts. */
    private Integer maxParticipants;

    /** Current participant count at the moment of the event. */
    private Integer currentParticipants;

    /** Time slot reference (only set when relevant). */
    private String timeSlotId;

    /**
     * For PARTICIPANT_JOINED / PARTICIPANT_LEFT, the user who joined/left.
     * For ACTIVITY_CREATED / ACTIVITY_CANCELLED this is null.
     */
    private String participantUserId;

    /** When the event happened on the producer side. */
    private LocalDateTime occurredAt;
}
