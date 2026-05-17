package com.sportlink.matchingservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

/**
 * Client for Scheduling Service.
 * Currently stubbed (sportlink.mock.scheduling=true in application.properties).
 * Used by the compatibility scorer to determine schedule overlap between two users.
 *
 * Switch mock to false and point sportlink.services.scheduling.url once
 * the Scheduling service is running.
 */
@Component
@Slf4j
public class SchedulingServiceClient {

    private final WebClient webClient;
    private final boolean mock;

    public SchedulingServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${sportlink.services.scheduling.url}") String baseUrl,
            @Value("${sportlink.mock.scheduling:true}") boolean mock) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.mock = mock;
    }

    /**
     * Resolve whether two users have overlapping availability.
     * Calls POST /api/timeslots/resolve in the Scheduling Service.
     *
     * @return a score 0.0–1.0: 1.0 = strong overlap, 0.0 = no overlap
     */
    public double resolveScheduleOverlap(UUID userAId, UUID userBId) {
        if (mock) {
            log.debug("Mock Scheduling: returning default schedule overlap for {} and {}", userAId, userBId);
            // Return a neutral mid-range score so scheduling doesn't dominate the result
            return 0.5;
        }

        // Real call — POST /api/timeslots/resolve with both user IDs
        ResolveRequest body = new ResolveRequest(List.of(userAId, userBId), null);
        ResolveResponse response = webClient.post()
                .uri("/api/timeslots/resolve")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(ResolveResponse.class)
                .block();

        if (response == null || response.getOverlappingSlots() == null || response.getOverlappingSlots().isEmpty()) {
            return 0.0;
        }
        // Normalize: more overlapping slots → higher score, capped at 1.0
        return Math.min(1.0, response.getOverlappingSlots().size() / 5.0);
    }

    // -------------------------------------------------------------------------
    // Local request/response DTOs for the Scheduling Service call
    // -------------------------------------------------------------------------

    public static class ResolveRequest {
        private List<UUID> userIds;
        private UUID facilityId; // optional

        public ResolveRequest(List<UUID> userIds, UUID facilityId) {
            this.userIds = userIds;
            this.facilityId = facilityId;
        }

        public List<UUID> getUserIds() { return userIds; }
        public UUID getFacilityId() { return facilityId; }
    }

    public static class ResolveResponse {
        private List<Object> overlappingSlots;

        public List<Object> getOverlappingSlots() { return overlappingSlots; }
        public void setOverlappingSlots(List<Object> overlappingSlots) {
            this.overlappingSlots = overlappingSlots;
        }
    }
}
