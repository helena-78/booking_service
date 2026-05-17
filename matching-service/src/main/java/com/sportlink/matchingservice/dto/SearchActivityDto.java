package com.sportlink.matchingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Represents a candidate activity entry returned by Search Service
 * (GET /api/search/activities). Contains pre-indexed activity fields
 * used for relevance scoring.
 */
@Data
@NoArgsConstructor
public class SearchActivityDto {
    private UUID activityId;
    private String sportType;
    private String activityStatus;      // PLANNED, ACTIVE, COMPLETED, CANCELLED
    private String city;
    private String district;
    private String skillLevel;          // skill band the activity targets
    private int maxParticipants;
}
