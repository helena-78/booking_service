package com.sportlink.moderation.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-side copy of Account Management's AccountEvent. Duplicated (instead
 * of shared via a JAR) so the two services stay decoupled at build time -
 * the contract is the JSON payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEvent {

    /** String, not enum: an unknown value from a newer producer won't blow up deserialization. */
    private String eventType;

    private UUID aggregateId;
    private String name;
    private String email;
    private String skillLevel;
    private String language;
    private String sportPreferences;
    private String city;
    private String district;
    private String userStatus;
    private String facilityStatus;
    private LocalDateTime occurredAt;
}
