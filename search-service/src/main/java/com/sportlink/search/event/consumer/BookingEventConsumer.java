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

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private static final String SLOT_KEYWORD_PREFIX = "slot:";

    private final SearchIndexRepository repository;

    @KafkaListener(
            topics = "${sportlink.kafka.topics.booking-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "bookingEventKafkaListenerContainerFactory"
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
            case "FACILITY_AVAILABILITY_CHANGED":
                refreshFacility(event);
                break;
            case "BOOKING_CREATED":
                log.debug("Ignoring BOOKING_CREATED for booking={} - " +
                        "not consumed by Search Service", event.getBookingId());
                break;
            default:
                log.debug("Ignoring unknown event type '{}' for booking={}",
                        event.getEventType(), event.getBookingId());
        }
    }

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

    private void refreshFacility(BookingEvent event) {
        String sportCenterId = event.getSportCenterId();
        if (sportCenterId == null || sportCenterId.isBlank()) {
            log.warn("FACILITY_AVAILABILITY_CHANGED with no sportCenterId; skipping");
            return;
        }
        Optional<SearchIndex> maybe = repository
                .findByEntityTypeAndEntityId(EntityType.FACILITY, sportCenterId);
        if (maybe.isEmpty()) {
            log.warn("Facility {} not present in search index; " +
                    "cannot refresh availability", sportCenterId);
            return;
        }
        SearchIndex idx = maybe.get();
        idx.setLastIndexedAt(LocalDateTime.now());
        repository.save(idx);
        log.info("Refreshed facility {} after availability change", sportCenterId);
    }

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
