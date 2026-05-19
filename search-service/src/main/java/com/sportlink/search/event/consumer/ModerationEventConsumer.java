package com.sportlink.search.event.consumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.event.ModerationEvent;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class ModerationEventConsumer {

    private static final String STATUS_KEYWORD_PREFIX = "status:";
    private static final String SANCTION_KEYWORD_PREFIX = "sanction:";

    private final SearchIndexRepository repository;

    @KafkaListener(
            topics = "${sportlink.kafka.topics.moderation-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "moderationEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void onModerationEvent(ModerationEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received moderation event with null type - dropping");
            return;
        }

        log.info("Received {} event for user={} case={} sanction={} status={}",
                event.getEventType(),
                event.getUserId(),
                event.getCaseId(),
                event.getSanctionType(),
                event.getUserStatus());

        switch (event.getEventType()) {
            case "USER_RESTRICTED":
                dropRestrictedUser(event);
                break;
            case "USER_STATUS_UPDATED":
                tagUserStatus(event);
                break;
            default:
                log.debug("Ignoring unknown event type '{}' for user={}",
                        event.getEventType(), event.getUserId());
        }
    }

    private void dropRestrictedUser(ModerationEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            log.warn("USER_RESTRICTED with no userId; skipping");
            return;
        }

        boolean existed = repository
                .existsByEntityTypeAndEntityId(EntityType.USER, event.getUserId());
        if (!existed) {
            log.debug("User {} not in search index; nothing to drop", event.getUserId());
            return;
        }

        repository.deleteByEntityTypeAndEntityId(EntityType.USER, event.getUserId());
        log.info("Removed user {} from search index after USER_RESTRICTED " +
                "(sanction={}, case={})",
                event.getUserId(), event.getSanctionType(), event.getCaseId());
    }

    private void tagUserStatus(ModerationEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            log.warn("USER_STATUS_UPDATED with no userId; skipping");
            return;
        }
        if (event.getUserStatus() == null || event.getUserStatus().isBlank()) {
            log.warn("USER_STATUS_UPDATED for user={} carries no status; skipping",
                    event.getUserId());
            return;
        }

        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.USER, event.getUserId());
        if (maybe.isEmpty()) {
            log.debug("User {} not in search index; nothing to tag", event.getUserId());
            return;
        }

        SearchIndex idx = maybe.get();
        List<String> kws = idx.getKeywords() != null
                ? new ArrayList<>(idx.getKeywords())
                : new ArrayList<>();

        kws.removeIf(kw -> kw != null
                && (kw.startsWith(STATUS_KEYWORD_PREFIX)
                        || kw.startsWith(SANCTION_KEYWORD_PREFIX)));

        kws.add(STATUS_KEYWORD_PREFIX + event.getUserStatus().toLowerCase());
        if (event.getSanctionType() != null && !event.getSanctionType().isBlank()) {
            kws.add(SANCTION_KEYWORD_PREFIX + event.getSanctionType().toLowerCase());
        }

        idx.setKeywords(kws);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Tagged user {} with status='{}' (sanction={})",
                event.getUserId(), event.getUserStatus(), event.getSanctionType());
    }
}
