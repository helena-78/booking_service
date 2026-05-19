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
public class AccountEvent {

    /**
     * One of: USER_UPDATED, USER_REPUTATION_UPDATED. Unknown future values
     * are ignored by the consumer.
     */
    private String eventType;

    /** Subject of the event. */
    private String userId;

    /** Display name to show in search results (only set on USER_UPDATED). */
    private String userName;

    /** Preferred / primary sport. */
    private String sportType;

    /** Skill level (BEGINNER / INTERMEDIATE / ADVANCED). */
    private String skillLevel;

    /** Location (e.g. "Tartu"). */
    private String location;

    /** Comma-separated sport preferences if the user has more than one. */
    private String userSportPreferences;

    /**
     * User reputation score (only set on USER_REPUTATION_UPDATED; null on
     * USER_UPDATED unless the upstream service decides to include it).
     */
    private Double userRatingScore;

    /** When the event happened on the producer side. */
    private LocalDateTime occurredAt;
}
