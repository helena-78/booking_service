package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingDeletedEvent {
    private UUID ratingId;
    private UUID revieweeId;
    private UUID activityId;
}
