package com.sportlink.ratingservice.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReputationUpdatedEvent {
    private UUID userId;
    private double averageScore;
    private String userLabel;
    private int totalRatings;
}