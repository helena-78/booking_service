package com.sportlink.matchingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Value object representing one candidate user in a suggestion list.
 * Stored as a JSONB element inside user_suggestions.suggested_users.
 * Also used as the response shape for the compatibility endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestedUser {

    private UUID userId;
    private double compatibilityScore;

    // Component scores — kept in the snapshot so callers can see the breakdown
    // without re-querying. The compatibility endpoint returns these too.
    private double sportScore;
    private double skillScore;
    private double locationScore;
    private double ratingScore;
    private double languageScore;
}
