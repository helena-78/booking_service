package com.sportlink.moderation.messaging;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.sportlink.moderation.event.ModerationEvent;
import com.sportlink.moderation.event.ModerationEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fire-and-forget: SanctionService persists the sanction first, then publishes.
 * A failed send is logged but never rolls back the HTTP response.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationEventPublisher {

    private final KafkaTemplate<String, ModerationEvent> kafkaTemplate;

    @Value("${sportlink.kafka.topics.moderation-events}")
    private String moderationEventsTopic;

    public void publishUserRestricted(ModerationEvent event) {
        publish(event);
    }

    private void publish(ModerationEvent event) {
        // Same eventType always lands on the same partition, so per-type
        // ordering is preserved without funnelling the whole topic to one.
        String key = event.getEventType() != null
                ? event.getEventType().name()
                : ModerationEventType.USER_RESTRICTED.name();

        CompletableFuture<SendResult<String, ModerationEvent>> future =
                kafkaTemplate.send(moderationEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} event for sanction {} (user {}): {}",
                        event.getEventType(), event.getSanctionId(),
                        event.getTargetUserId(), ex.getMessage(), ex);
            } else {
                log.info("Published {} event for sanction {} (user {}) to topic '{}' partition {} offset {}",
                        event.getEventType(),
                        event.getSanctionId(),
                        event.getTargetUserId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
