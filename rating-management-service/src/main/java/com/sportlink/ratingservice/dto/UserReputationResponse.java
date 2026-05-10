package com.sportlink.ratingservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserReputationResponse {
    private UUID userId;
    private double averageScore;
    private String userLabel;
    private int totalRatings;
    private LocalDateTime lastUpdatedAt;
}