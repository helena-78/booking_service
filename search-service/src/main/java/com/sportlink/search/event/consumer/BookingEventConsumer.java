package com.sportlink.search.event.consumer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.event.BookingEvent;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Consumes booking-events from Kafka and updates the search index so
 * facility availability stays in sync with the Booking Service.
 *
 * Per the Checkpoint 3 event table (Workflow 3 + Workflow 4), the Search
 * Service is interested in three of the four event types:
 *
 *   BOOKING_CONFIRMED  -  slot becomes occupied  -> add timeSlotId to
 *                                                   the facility's
 *                                                   "occupiedSlots" keyword set
 *   BOOKING_CANCELLED  -  slot becomes free      -> remove timeSlotId
 *   PAYMENT_REFUNDED   -  reinforces cancelled   -> remove timeSlotId
 *                                                   (idempotent reinforcement)
 *
 * BOOKING_CREATED is ignored - per the assignment table only the
 * Notification Service consumes it. We log it at DEBUG level for traceability.
 *
 * Strategy: the facility's SearchIndex row is looked up by entityId, which
 * the Booking Service event carries as sportCenterId. The "occupied slot"
 * marker is stored as a synthetic keyword of the form "slot:{timeSlotId}".
 * This keeps the existing schema unchanged - no new column needed - and
 * the keywords are already searchable via the matchesText() filter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private static final String SLOT_KEYWORD_PREFIX = "slot:";

    private final SearchIndexRepository repository;

    @KafkaListener(
            topics = "${sportlink.kafka.topics.booking-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    @Transactional
    public void onBookingEvent(BookingEvent event) {
        if (event == null || event.getEventType() == null) {
            log.warn("Received booking event with null type - dropping");
            return;
        }

        log.info("Received {} event for booking={} sportCenter={} activity={} slot={}",
                event.getEventType(),
                event.getBookingId(),
                event.getSportCenterId(),
                event.getActivityId(),
                event.getTimeSlotId());

        switch (event.getEventType()) {
            case "BOOKING_CONFIRMED":
                markSlotOccupied(event);
                break;
            case "BOOKING_CANCELLED":
            case "PAYMENT_REFUNDED":
                markSlotFree(event);
                break;
            case "BOOKING_CREATED":
                // Not in this service's responsibilities (assignment table -
                // Notification Service handles WF 1). Logged for traceability.
                log.debug("Ignoring BOOKING_CREATED for booking={} - " +
                        "not consumed by Search Service", event.getBookingId());
                break;
            default:
                log.debug("Ignoring unknown event type '{}' for booking={}",
                        event.getEventType(), event.getBookingId());
        }
    }

    // ----------------------------------------------------------------
    // event handlers
    // ----------------------------------------------------------------

    private void markSlotOccupied(BookingEvent event) {
        Optional<SearchIndex> maybe = findFacilityIndex(event);
        if (maybe.isEmpty()) return;

        SearchIndex idx = maybe.get();
        String keyword = SLOT_KEYWORD_PREFIX + event.getTimeSlotId();
        List<String> kws = idx.getKeywords() != null
                ? new ArrayList<>(idx.getKeywords())
                : new ArrayList<>();

        if (kws.contains(keyword)) {
            log.debug("Slot {} already marked occupied on facility {}; no-op",
                    event.getTimeSlotId(), event.getSportCenterId());
            return;
        }

        kws.add(keyword);
        idx.setKeywords(kws);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Marked slot {} as OCCUPIED on facility {} (search index updated)",
                event.getTimeSlotId(), event.getSportCenterId());
    }

    private void markSlotFree(BookingEvent event) {
        Optional<SearchIndex> maybe = findFacilityIndex(event);
        if (maybe.isEmpty()) return;

        SearchIndex idx = maybe.get();
        String keyword = SLOT_KEYWORD_PREFIX + event.getTimeSlotId();
        List<String> kws = idx.getKeywords();

        if (kws == null || !kws.contains(keyword)) {
            log.debug("Slot {} was not marked occupied on facility {}; no-op",
                    event.getTimeSlotId(), event.getSportCenterId());
            return;
        }

        List<String> updated = new ArrayList<>(kws);
        updated.remove(keyword);
        idx.setKeywords(updated);
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Marked slot {} as FREE on facility {} (search index updated)",
                event.getTimeSlotId(), event.getSportCenterId());
    }

    // ----------------------------------------------------------------
    // helpers
    // ----------------------------------------------------------------

    private Optional<SearchIndex> findFacilityIndex(BookingEvent event) {
        String sportCenterId = event.getSportCenterId();
        if (sportCenterId == null || sportCenterId.isBlank()) {
            log.warn("Event {} for booking={} has no sportCenterId; skipping",
                    event.getEventType(), event.getBookingId());
            return Optional.empty();
        }
        if (event.getTimeSlotId() == null || event.getTimeSlotId().isBlank()) {
            log.warn("Event {} for booking={} has no timeSlotId; skipping",
                    event.getEventType(), event.getBookingId());
            return Optional.empty();
        }

        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.FACILITY, sportCenterId);
        if (maybe.isEmpty()) {
            log.warn("Facility {} not present in search index; " +
                    "cannot apply {}. Events are best-effort; will not retry.",
                    sportCenterId, event.getEventType());
        }
        return maybe;
    }
}
