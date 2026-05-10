package com.sportlink.ratingservice.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class RatingRequest {
    private UUID reviewerId;
    private UUID revieweeId;
    private UUID activityId;
    private int behaviorValue;       // e.g. 1–5
    private String behaviorLabel;    // e.g. "FAIR_PLAY"
    private int skillValue;
    private String skillLabel;       // e.g. "INTERMEDIATE"
}