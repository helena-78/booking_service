package com.sportlink.matchingservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Represents a user profile as returned by Account Management Service.
 * Only includes the fields the Matching Service needs for scoring.
 * Fields map to the user_profiles table in Account Management.
 */
@Data
@NoArgsConstructor
public class UserProfileDto {

    private UUID userId;
    private String name;
    private String skillLevel;          // BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL
    private String language;
    private List<String> sportPreferences; // ["badminton", "football"]

    // Embedded UserLocation value object
    private String city;
    private String district;

    // Embedded UserLabel value object — populated from Rating Management
    private String behaviorLabel;
    private double labelValue;

    // Status — we skip SUSPENDED/BANNED users from suggestions
    private String status;              // ACTIVE, SUSPENDED, BANNED
}
