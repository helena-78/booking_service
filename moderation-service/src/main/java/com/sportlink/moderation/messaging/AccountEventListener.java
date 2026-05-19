package com.sportlink.moderation.messaging;

import com.sportlink.moderation.event.AccountEvent;

import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Account Management publishes every event subtype to the same topic, so
 * we filter in code. Today we only react to USER_STATUS_UPDATED; the rest
 * are dropped at debug level.
 */
@Component
@Slf4j
public class AccountEventListener {

    private static final String USER_STATUS_UPDATED = "USER_STATUS_UPDATED";

    @KafkaListener(
            topics = "${sportlink.kafka.topics.account-events}",
            groupId = "${spring.kafka.consumer.group-id:moderation-service}")
    public void onAccountEvent(AccountEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received malformed Account event (null or missing eventType) - ignoring");
            return;
        }

        if (USER_STATUS_UPDATED.equals(event.getEventType())) {
            log.info("Received UserStatusUpdated event from Account Management - user {} is now {}",
                    event.getAggregateId(), event.getUserStatus());
            return;
        }

        log.debug("Ignoring Account event of type {} (aggregate {})",
                event.getEventType(), event.getAggregateId());
    }
}
