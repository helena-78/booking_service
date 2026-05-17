package com.sportlink.matchingservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Returned by GET /api/matching/compatibility?userAId=...&userBId=...
 * Not persisted — computed on demand.
 */
@Data
@Builder
public class CompatibilityResponse {
    private UUID userAId;
    private UUID userBId;
    private double compatibilityScore;

    // Component breakdown
    private double sportScore;
    private double skillScore;
    private double locationScore;
    private double ratingScore;
    private double languageScore;
}
