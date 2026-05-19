package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingCreatedEvent {
    private UUID ratingId;
    private UUID reviewerId;
    private UUID revieweeId;
    private UUID activityId;
    private int behaviorValue;
    private int skillValue;
}
