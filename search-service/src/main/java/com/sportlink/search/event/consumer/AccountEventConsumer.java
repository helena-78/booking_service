package com.sportlink.search.event.consumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.event.AccountEvent;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchFilters;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class AccountEventConsumer {

    private final SearchIndexRepository repository;

    @KafkaListener(
            topics = "${sportlink.kafka.topics.account-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "accountEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void onAccountEvent(AccountEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received account event with null type - dropping");
            return;
        }

        log.info("Received {} event for user={} sport={} skill={} rating={}",
                event.getEventType(),
                event.getUserId(),
                event.getSportType(),
                event.getSkillLevel(),
                event.getUserRatingScore());

        switch (event.getEventType()) {
            case "USER_UPDATED":
                upsertUser(event);
                break;
            case "USER_REPUTATION_UPDATED":
                updateReputation(event);
                break;
            default:
                log.debug("Ignoring unknown event type '{}' for user={}",
                        event.getEventType(), event.getUserId());
        }
    }

    private void upsertUser(AccountEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            log.warn("USER_UPDATED with no userId; skipping");
            return;
        }

        Optional<SearchIndex> existing = repository
                .findByEntityTypeAndEntityId(EntityType.USER, event.getUserId());

        SearchIndex idx = existing.orElseGet(() -> SearchIndex.builder()
                .indexId(UUID.randomUUID().toString())
                .entityType(EntityType.USER)
                .entityId(event.getUserId())
                .keywords(new ArrayList<>())
                .build());

        if (event.getUserName() != null) idx.setDisplayName(event.getUserName());
        idx.setLastIndexedAt(LocalDateTime.now());

        SearchFilters f = idx.getFilters() != null ? idx.getFilters() : new SearchFilters();
        if (event.getUserName() != null) f.setUserName(event.getUserName());
        if (event.getSportType() != null) f.setSportType(event.getSportType());
        if (event.getSkillLevel() != null) f.setSkillLevel(event.getSkillLevel());
        if (event.getLocation() != null) f.setLocation(event.getLocation());
        if (event.getUserSportPreferences() != null) {
            f.setUserSportPreferences(event.getUserSportPreferences());
        }
        if (event.getUserRatingScore() != null) {
            f.setUserRatingScore(event.getUserRatingScore());
        }
        idx.setFilters(f);

        List<String> kept = new ArrayList<>();
        if (idx.getKeywords() != null) {
            for (String kw : idx.getKeywords()) {
                if (kw != null && (kw.startsWith("status:") || kw.startsWith("sanction:"))) {
                    kept.add(kw);
                }
            }
        }
        if (event.getUserName() != null) kept.add(event.getUserName().toLowerCase());
        if (event.getSportType() != null) kept.add(event.getSportType().toLowerCase());
        if (event.getLocation() != null) kept.add(event.getLocation().toLowerCase());
        idx.setKeywords(kept);

        repository.save(idx);
        log.info("Upserted USER index entry for user={} (name='{}')",
                event.getUserId(), event.getUserName());
    }

    private void updateReputation(AccountEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            log.warn("USER_REPUTATION_UPDATED with no userId; skipping");
            return;
        }
        if (event.getUserRatingScore() == null) {
            log.warn("USER_REPUTATION_UPDATED for user={} carries no score; skipping",
                    event.getUserId());
            return;
        }

        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.USER, event.getUserId());
        if (maybe.isEmpty()) {
            log.warn("User {} not present in search index; cannot apply reputation update. " +
                    "Will rely on a later USER_UPDATED event to create the entry.",
                    event.getUserId());
            return;
        }

        SearchIndex idx = maybe.get();
        SearchFilters f = idx.getFilters() != null ? idx.getFilters() : new SearchFilters();
        f.setUserRatingScore(event.getUserRatingScore());
        idx.setFilters(f);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Updated reputation for user={} to score={}",
                event.getUserId(), event.getUserRatingScore());
    }
}
