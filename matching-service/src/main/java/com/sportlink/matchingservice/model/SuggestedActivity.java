package com.sportlink.matchingservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Value object representing one candidate activity in a suggestion list.
 * Stored as a JSONB element inside activity_suggestions.suggested_activities.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestedActivity {

    private UUID activityId;
    private double relevanceScore;

    // Component scores for transparency
    private double sportScore;
    private double skillScore;
    private double locationScore;
}
