package com.sportlink.activitymanagement.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sportlink.activitymanagement.config.KafkaTopicConfiguration;
import com.sportlink.activitymanagement.event.ActivityCancelledEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Publishes Activity domain events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityEventPublisher {

    private final KafkaTemplate<String, ActivityCancelledEvent> kafkaTemplate;

    public void publishActivityCancelled(ActivityCancelledEvent event) {
        log.info("Publishing ActivityCancelledEvent for activity {} (slot={})",
                event.getActivityId(), event.getPreferredTimeSlotId());
        kafkaTemplate.send(KafkaTopicConfiguration.ACTIVITY_CANCELLED_TOPIC, event);
    }
}