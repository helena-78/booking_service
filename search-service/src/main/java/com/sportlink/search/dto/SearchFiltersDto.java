package com.sportlink.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Held inside a SearchIndexDto and supplied by clients when indexing.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchFiltersDto {
    private String sportType;
    private String location;
    private String skillLevel;
    private String activityTimeSlots;
    private String activityTitle;
    private String activityStatus;
    private Integer activityMaxParticipants;
    private String userName;
    private Double userRatingScore;
    private String userSportPreferences;
    private String facilityName;
}
