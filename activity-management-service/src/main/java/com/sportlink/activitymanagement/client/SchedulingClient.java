package com.sportlink.activitymanagement.client;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Thin client over Scheduling Service.
 *
 * Activity Management uses it to reserve a time slot when an activity is created
 * with a preferredTimeSlotId, and to release the slot when the activity is cancelled.
 */
@Component
@Slf4j
public class SchedulingClient {

    private final WebClient.Builder webClientBuilder;
    private final String schedulingUrl;
    private final boolean mock;

    public SchedulingClient(WebClient.Builder webClientBuilder,
                            @Value("${sportlink.services.scheduling.url:http://localhost:8087}") String schedulingUrl,
                            @Value("${sportlink.mock.dependencies:false}") boolean mock) {
        this.webClientBuilder = webClientBuilder;
        this.schedulingUrl = schedulingUrl;
        this.mock = mock;
    }

    /**
     * Reserve a time slot for an activity.
     * Returns the slotId on success, null if the call failed (graceful fallback).
     */
    public UUID reserveSlot(LocalDateTime startTime, LocalDateTime endTime,
                            UUID organizerId, UUID activityId) {
        if (mock) {
            log.debug("Mock mode: pretending to reserve slot for activity {}", activityId);
            return UUID.randomUUID();
        }

        try {
            Map<String, Object> request = Map.of(
                    "startTime", startTime.toString(),
                    "endTime", endTime.toString(),
                    "organizerId", organizerId.toString(),
                    "reservedForId", activityId.toString()
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = webClientBuilder.build()
                    .post()
                    .uri(schedulingUrl + "/api/timeslots/reserve")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("slotId") != null) {
                UUID slotId = UUID.fromString(response.get("slotId").toString());
                log.info("Reserved slot {} for activity {}", slotId, activityId);
                return slotId;
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to reserve slot for activity {}: {}. Continuing without reservation.",
                    activityId, e.getMessage());
            return null;
        }
    }

    /**
     * Release a previously reserved time slot.
     * Idempotent and best-effort — failures are logged but do not propagate.
     */
    public void releaseSlot(UUID slotId) {
        if (mock) {
            log.debug("Mock mode: pretending to release slot {}", slotId);
            return;
        }

        try {
            webClientBuilder.build()
                    .delete()
                    .uri(schedulingUrl + "/api/timeslots/{id}/release", slotId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("Released slot {}", slotId);
        } catch (Exception e) {
            log.warn("Failed to release slot {}: {}", slotId, e.getMessage());
        }
    }
}