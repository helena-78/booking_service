package com.sportlink.matchingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Represents a candidate user entry returned by Search Service
 * (GET /api/search/users). Contains pre-indexed profile fields
 * used to filter candidates before full scoring.
 */
@Data
@NoArgsConstructor
public class SearchUserDto {
    private UUID userId;
    private String skillLevel;
    private String city;
    private String district;
    private List<String> sportPreferences;
    private double userRatingScore;
}
