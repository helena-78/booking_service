package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReputationUpdatedEvent {
    private UUID userId;
    private double averageScore;
    private String userLabel;
    private int totalRatings;
}
