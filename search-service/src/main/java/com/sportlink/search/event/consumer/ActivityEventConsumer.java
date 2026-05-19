package com.sportlink.search.event.consumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.event.ActivityEvent;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchFilters;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityEventConsumer {

    private static final String PARTICIPANT_KEYWORD_PREFIX = "participant:";

    private final SearchIndexRepository repository;

    @KafkaListener(
            topics = "${sportlink.kafka.topics.activity-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "activityEventKafkaListenerContainerFactory"
    )
    @Transactional
    public void onActivityEvent(ActivityEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received activity event with null type - dropping");
            return;
        }

        log.info("Received {} event for activity={} sport={} location={} status={}",
                event.getEventType(),
                event.getActivityId(),
                event.getSportType(),
                event.getLocation(),
                event.getActivityStatus());

        switch (event.getEventType()) {
            case "ACTIVITY_CREATED":
                upsertActivity(event);
                break;
            case "ACTIVITY_CANCELLED":
                markActivityCancelled(event);
                break;
            case "PARTICIPANT_JOINED":
                addParticipant(event);
                break;
            case "PARTICIPANT_LEFT":
                removeParticipant(event);
                break;
            default:
                log.debug("Ignoring unknown event type '{}' for activity={}",
                        event.getEventType(), event.getActivityId());
        }
    }

    private void upsertActivity(ActivityEvent event) {
        if (event.getActivityId() == null || event.getActivityId().isBlank()) {
            log.warn("ACTIVITY_CREATED with no activityId; skipping");
            return;
        }

        Optional<SearchIndex> existing = repository
                .findByEntityTypeAndEntityId(EntityType.ACTIVITY, event.getActivityId());

        SearchIndex idx = existing.orElseGet(() -> SearchIndex.builder()
                .indexId(UUID.randomUUID().toString())
                .entityType(EntityType.ACTIVITY)
                .entityId(event.getActivityId())
                .keywords(new ArrayList<>())
                .build());

        idx.setDisplayName(event.getActivityTitle() != null
                ? event.getActivityTitle()
                : idx.getDisplayName());
        idx.setLastIndexedAt(LocalDateTime.now());

        SearchFilters f = idx.getFilters() != null ? idx.getFilters() : new SearchFilters();
        if (event.getActivityTitle() != null) f.setActivityTitle(event.getActivityTitle());
        if (event.getSportType() != null) f.setSportType(event.getSportType());
        if (event.getLocation() != null) f.setLocation(event.getLocation());
        if (event.getActivityStatus() != null) f.setActivityStatus(event.getActivityStatus());
        if (event.getMaxParticipants() != null) f.setActivityMaxParticipants(event.getMaxParticipants());
        if (event.getTimeSlotId() != null) f.setActivityTimeSlots(event.getTimeSlotId());
        idx.setFilters(f);

        List<String> kept = new ArrayList<>();
        if (idx.getKeywords() != null) {
            for (String kw : idx.getKeywords()) {
                if (kw != null && kw.startsWith(PARTICIPANT_KEYWORD_PREFIX)) {
                    kept.add(kw);
                }
            }
        }
        if (event.getSportType() != null) kept.add(event.getSportType().toLowerCase());
        if (event.getLocation() != null) kept.add(event.getLocation().toLowerCase());
        if (event.getActivityTitle() != null) kept.add(event.getActivityTitle().toLowerCase());
        idx.setKeywords(kept);

        repository.save(idx);
        log.info("Upserted ACTIVITY index entry for activity={} (title='{}')",
                event.getActivityId(), event.getActivityTitle());
    }

    private void markActivityCancelled(ActivityEvent event) {
        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.ACTIVITY, event.getActivityId());
        if (maybe.isEmpty()) {
            log.warn("Activity {} not present in search index; cannot mark cancelled",
                    event.getActivityId());
            return;
        }
        SearchIndex idx = maybe.get();
        SearchFilters f = idx.getFilters() != null ? idx.getFilters() : new SearchFilters();
        f.setActivityStatus("CANCELLED");
        idx.setFilters(f);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Marked activity {} as CANCELLED in search index", event.getActivityId());
    }

    private void addParticipant(ActivityEvent event) {
        if (event.getParticipantUserId() == null || event.getParticipantUserId().isBlank()) {
            log.warn("PARTICIPANT_JOINED with no participantUserId; skipping (activity={})",
                    event.getActivityId());
            return;
        }
        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.ACTIVITY, event.getActivityId());
        if (maybe.isEmpty()) {
            log.warn("Activity {} not present in search index; cannot add participant",
                    event.getActivityId());
            return;
        }
        SearchIndex idx = maybe.get();
        String keyword = PARTICIPANT_KEYWORD_PREFIX + event.getParticipantUserId();
        List<String> kws = idx.getKeywords() != null
                ? new ArrayList<>(idx.getKeywords())
                : new ArrayList<>();
        if (kws.contains(keyword)) {
            log.debug("User {} already a participant on activity {}; no-op",
                    event.getParticipantUserId(), event.getActivityId());
            return;
        }
        kws.add(keyword);
        idx.setKeywords(kws);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Added participant {} to activity {} (now {} participants)",
                event.getParticipantUserId(),
                event.getActivityId(),
                participantCount(kws));
    }

    private void removeParticipant(ActivityEvent event) {
        if (event.getParticipantUserId() == null || event.getParticipantUserId().isBlank()) {
            log.warn("PARTICIPANT_LEFT with no participantUserId; skipping (activity={})",
                    event.getActivityId());
            return;
        }
        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.ACTIVITY, event.getActivityId());
        if (maybe.isEmpty()) {
            log.warn("Activity {} not present in search index; cannot remove participant",
                    event.getActivityId());
            return;
        }
        SearchIndex idx = maybe.get();
        String keyword = PARTICIPANT_KEYWORD_PREFIX + event.getParticipantUserId();
        List<String> kws = idx.getKeywords();
        if (kws == null || !kws.contains(keyword)) {
            log.debug("User {} was not a participant on activity {}; no-op",
                    event.getParticipantUserId(), event.getActivityId());
            return;
        }
        List<String> updated = new ArrayList<>(kws);
        updated.remove(keyword);
        idx.setKeywords(updated);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Removed participant {} from activity {} (now {} participants)",
                event.getParticipantUserId(),
                event.getActivityId(),
                participantCount(updated));
    }

    private long participantCount(List<String> keywords) {
        if (keywords == null) return 0;
        return keywords.stream()
                .filter(kw -> kw != null && kw.startsWith(PARTICIPANT_KEYWORD_PREFIX))
                .count();
    }
}
