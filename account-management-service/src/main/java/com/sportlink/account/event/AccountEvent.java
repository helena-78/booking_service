package com.sportlink.account.event;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Single envelope for everything published to the 'account-events' topic.
 * Subtype lives in {@link #eventType}; fields irrelevant to a given subtype
 * stay null and are stripped from the JSON payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountEvent {

    private AccountEventType eventType;

    /** userId for user events, centerId for facility events. */
    private UUID aggregateId;

    private String name;
    private String email;
    private String skillLevel;
    private String language;
    private String sportPreferences;
    private String city;
    private String district;

    /** Populated on USER_STATUS_UPDATED. */
    private String userStatus;

    /** Populated on FACILITY_PROFILE_UPDATED. */
    private String facilityStatus;

    private LocalDateTime occurredAt;
}
