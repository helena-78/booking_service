package com.sportlink.account.messaging;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import com.sportlink.account.event.AccountEvent;
import com.sportlink.account.event.AccountEventType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fire-and-forget: callers persist their state change first, then publish.
 * A failed send is logged but never rolls back the HTTP response.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventPublisher {

    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;

    @Value("${sportlink.kafka.topics.account-events}")
    private String accountEventsTopic;

    public void publishUserProfileUpdated(AccountEvent event) {
        publish(event);
    }

    public void publishUserStatusUpdated(AccountEvent event) {
        publish(event);
    }

    public void publishFacilityProfileUpdated(AccountEvent event) {
        publish(event);
    }

    private void publish(AccountEvent event) {
        // Same eventType always lands on the same partition, so per-type
        // ordering is preserved without funnelling the whole topic to one.
        String key = event.getEventType() != null
                ? event.getEventType().name()
                : AccountEventType.USER_PROFILE_UPDATED.name();

        CompletableFuture<SendResult<String, AccountEvent>> future =
                kafkaTemplate.send(accountEventsTopic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish {} event for aggregate {}: {}",
                        event.getEventType(), event.getAggregateId(), ex.getMessage(), ex);
            } else {
                log.info("Published {} event for aggregate {} to topic '{}' partition {} offset {}",
                        event.getEventType(),
                        event.getAggregateId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
