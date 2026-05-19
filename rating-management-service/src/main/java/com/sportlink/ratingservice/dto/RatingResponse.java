package com.sportlink.ratingservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatingResponse {
    private UUID ratingId;
    private UUID reviewerId;
    private String reviewerName;
    private UUID revieweeId;
    private String revieweeName;
    private UUID activityId;
    private int behaviorValue;
    private String behaviorLabel;
    private int skillValue;
    private String skillLabel;
    private LocalDateTime createdAt;
}
