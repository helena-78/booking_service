package com.sportlink.matchingservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdatedEvent {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String city;
    private String district;
    private String skillLevel;
    private java.util.List<String> sportPreferences;
    private String status;
}
