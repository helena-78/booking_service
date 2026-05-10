package com.sportlink.ratingservice.stub;

import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class ActivityManagementStub {

    /**
     * Validates that both users participated in the activity AND it is COMPLETED.
     * STUB: always returns true. Replace with RestTemplate call later.
     */
    public boolean bothParticipatedAndActivityCompleted(UUID activityId, UUID reviewerId, UUID revieweeId) {
        return true;
    }
}