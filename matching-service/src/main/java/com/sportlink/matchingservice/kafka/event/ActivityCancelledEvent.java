package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCancelledEvent {
    private UUID activityId;
    private UUID organizerId;
    private String reason;
}
