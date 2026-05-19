package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCreatedEvent {
    private UUID activityId;
    private UUID organizerId;
    private String activityName;
    private String sport;
    private String skillLevel;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int maxParticipants;
    private String status;
}
