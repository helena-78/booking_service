package com.sportlink.search.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchFilters {

    @Column(name = "sport_type")
    private String sportType;

    @Column(name = "location")
    private String location;

    @Column(name = "skill_level")
    private String skillLevel;

    @Column(name = "activity_time_slots")
    private String activityTimeSlots;

    @Column(name = "activity_title")
    private String activityTitle;

    @Column(name = "activity_status")
    private String activityStatus;

    @Column(name = "activity_max_participants")
    private Integer activityMaxParticipants;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_rating_score")
    private Double userRatingScore;

    @Column(name = "user_sport_preferences")
    private String userSportPreferences;

    @Column(name = "facility_name")
    private String facilityName;
}
