package com.sportlink.scheduling.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.sportlink.scheduling.event.ActivityCancelledEvent;
import com.sportlink.scheduling.exception.TimeSlotNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Listens to activity domain events from Kafka.
 *
 * When an activity is cancelled (event from Activity Management), this
 * consumer releases the reserved time slot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityEventConsumer {

    private final SchedulingService schedulingService;

    @KafkaListener(
            topics = "activity-cancelled-events",
            groupId = "scheduling-service"
    )
    public void onActivityCancelled(ActivityCancelledEvent event) {
        log.info("Received ActivityCancelledEvent for activity {} (slot={})",
                event.getActivityId(), event.getPreferredTimeSlotId());

        if (event.getPreferredTimeSlotId() == null) {
            log.info("No slot was reserved for activity {} — nothing to release",
                    event.getActivityId());
            return;
        }

        try {
            schedulingService.releaseTimeSlot(event.getPreferredTimeSlotId());
            log.info("Released slot {} after activity {} was cancelled",
                    event.getPreferredTimeSlotId(), event.getActivityId());
        } catch (TimeSlotNotFoundException e) {
            // The slot may not exist in this DB (e.g. activity created
            // before Scheduling was integrated). Safe to ignore — the
            // event is consumed and we move on.
            log.warn("Slot {} from event not found — already gone, ignoring",
                    event.getPreferredTimeSlotId());
        } catch (Exception e) {
            log.error("Failed to release slot {} for cancelled activity {}: {}",
                    event.getPreferredTimeSlotId(), event.getActivityId(), e.getMessage());
            // Re-throw so Kafka considers this delivery failed and may
            // retry (default Spring Kafka behaviour: SeekToCurrentErrorHandler).
            throw e;
        }
    }
}