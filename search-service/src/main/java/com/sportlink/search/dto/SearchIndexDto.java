package com.sportlink.search.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.sportlink.search.model.EntityType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned by the search endpoints.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchIndexDto {

    private String indexId;
    private EntityType entityType;
    private String entityId;
    private String displayName;
    @Builder.Default
    private List<String> keywords = new ArrayList<>();
    private LocalDateTime lastIndexedAt;

    // ---- flattened SearchFilters fields (top-level for convenience) ----
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

    // ---- structured filters echo (for clients that prefer the nested object) ----
    private SearchFiltersDto filters;
}
