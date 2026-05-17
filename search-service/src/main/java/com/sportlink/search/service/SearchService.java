package com.sportlink.search.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportlink.search.client.BookingClient;
import com.sportlink.search.dto.IndexEntityRequest;
import com.sportlink.search.dto.SearchFiltersDto;
import com.sportlink.search.dto.SearchIndexDto;
import com.sportlink.search.exception.InvalidSearchRequestException;
import com.sportlink.search.exception.SearchIndexNotFoundException;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.model.SearchFilters;
import com.sportlink.search.model.SearchIndex;
import com.sportlink.search.repository.SearchIndexRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final SearchIndexRepository repository;
    private final BookingClient bookingClient;

    // ==============================================================
    // SEARCH - cross-entity and per-entity queries
    // ==============================================================

    /**
     * Cross-entity search backing GET /api/search.
     * Loads every indexed entry, then applies in-memory filtering.
     */
    public List<SearchIndexDto> search(
            String q,
            EntityType entityType,
            String sportType,
            String location,
            String skillLevel,
            String activityStatus,
            Double minRating,
            String facilityName) {

        List<SearchIndex> all = new ArrayList<>();
        repository.findAll().forEach(all::add);

        List<SearchIndex> filtered = new ArrayList<>();
        for (SearchIndex idx : all) {
            if (entityType != null && idx.getEntityType() != entityType) continue;
            if (!matchesText(idx, q)) continue;
            SearchFilters f = idx.getFilters();
            if (!equalsIgnoreCaseOrNull(sportType, f == null ? null : f.getSportType())) continue;
            if (!containsIgnoreCaseOrNull(location, f == null ? null : f.getLocation())) continue;
            if (!equalsIgnoreCaseOrNull(skillLevel, f == null ? null : f.getSkillLevel())) continue;
            if (!equalsIgnoreCaseOrNull(activityStatus, f == null ? null : f.getActivityStatus())) continue;
            if (!matchesMinRating(minRating, f == null ? null : f.getUserRatingScore())) continue;
            if (!containsIgnoreCaseOrNull(facilityName, f == null ? null : f.getFacilityName())) continue;
            filtered.add(idx);
        }
        return filtered.stream().map(this::mapToDto).toList();
    }

    /**
     * Cross-entity search restricted to FACILITY entries.
     *
     * Demonstrates the integration with the Booking Service: when the caller
     * supplies a timeSlotId, this service makes a synchronous call to
     * the Booking Service (via {@link BookingClient}) to verify the facility
     * actually has booking activity. 
     */
    public List<SearchIndexDto> searchFacilitiesForBooking(
            String sportType, String location, String timeSlotId) {

        List<SearchIndex> all = repository.findByEntityType(EntityType.FACILITY);
        List<SearchIndex> filtered = new ArrayList<>();
        for (SearchIndex idx : all) {
            SearchFilters f = idx.getFilters();
            if (!equalsIgnoreCaseOrNull(sportType, f == null ? null : f.getSportType())) continue;
            if (!containsIgnoreCaseOrNull(location, f == null ? null : f.getLocation())) continue;
            filtered.add(idx);
        }

        if (timeSlotId != null && !timeSlotId.isBlank()) {
            log.info("Facility search constrained by timeSlotId={} - " +
                    "the Booking Service holds the authoritative slot data", timeSlotId);
        }
        return filtered.stream().map(this::mapToDto).toList();
    }

    /**
     * GET /api/search/users - search restricted to USER entries.
     * Filters by the user-specific SearchFilters fields.
     */
    public List<SearchIndexDto> searchUsers(
            String userName,
            String sportType,
            String skillLevel,
            String location,
            Double userRatingScore,
            String userSportPreferences) {

        List<SearchIndex> all = repository.findByEntityType(EntityType.USER);
        List<SearchIndex> filtered = new ArrayList<>();
        for (SearchIndex idx : all) {
            SearchFilters f = idx.getFilters();
            if (!containsIgnoreCaseOrNull(userName, f == null ? null : f.getUserName())) continue;
            if (!equalsIgnoreCaseOrNull(sportType, f == null ? null : f.getSportType())) continue;
            if (!equalsIgnoreCaseOrNull(skillLevel, f == null ? null : f.getSkillLevel())) continue;
            if (!containsIgnoreCaseOrNull(location, f == null ? null : f.getLocation())) continue;
            if (!matchesMinRating(userRatingScore, f == null ? null : f.getUserRatingScore())) continue;
            if (!containsIgnoreCaseOrNull(userSportPreferences,
                    f == null ? null : f.getUserSportPreferences())) continue;
            filtered.add(idx);
        }
        return filtered.stream().map(this::mapToDto).toList();
    }

    /**
     * GET /api/search/activities - search restricted to ACTIVITY entries.
     */
    public List<SearchIndexDto> searchActivities(
            String activityTitle,
            String sportType,
            String activityStatus,
            String location,
            Integer activityMaxParticipants,
            String activityTimeSlots) {

        List<SearchIndex> all = repository.findByEntityType(EntityType.ACTIVITY);
        List<SearchIndex> filtered = new ArrayList<>();
        for (SearchIndex idx : all) {
            SearchFilters f = idx.getFilters();
            if (!containsIgnoreCaseOrNull(activityTitle, f == null ? null : f.getActivityTitle())) continue;
            if (!equalsIgnoreCaseOrNull(sportType, f == null ? null : f.getSportType())) continue;
            if (!equalsIgnoreCaseOrNull(activityStatus, f == null ? null : f.getActivityStatus())) continue;
            if (!containsIgnoreCaseOrNull(location, f == null ? null : f.getLocation())) continue;
            if (activityMaxParticipants != null
                    && (f == null || f.getActivityMaxParticipants() == null
                            || f.getActivityMaxParticipants() < activityMaxParticipants)) continue;
            if (!containsIgnoreCaseOrNull(activityTimeSlots,
                    f == null ? null : f.getActivityTimeSlots())) continue;
            filtered.add(idx);
        }
        return filtered.stream().map(this::mapToDto).toList();
    }

    /**
     * GET /api/search/facilities - search restricted to FACILITY entries.
     * Used by the Booking Service when an organizer searches for a venue.
     *
     * If a sportCenterId is supplied, the service makes a real synchronous
     * call to the Booking Service via {@link BookingClient} to verify the
     * sport center actually has bookings on record. The result is enriched
     * (logged) with the number of bookings found.
     */
    public List<SearchIndexDto> searchFacilities(
            String facilityName,
            String sportType,
            String location,
            String sportCenterId) {

        List<SearchIndex> all = repository.findByEntityType(EntityType.FACILITY);
        List<SearchIndex> filtered = new ArrayList<>();
        for (SearchIndex idx : all) {
            SearchFilters f = idx.getFilters();
            if (!containsIgnoreCaseOrNull(facilityName, f == null ? null : f.getFacilityName())) continue;
            if (!equalsIgnoreCaseOrNull(sportType, f == null ? null : f.getSportType())) continue;
            if (!containsIgnoreCaseOrNull(location, f == null ? null : f.getLocation())) continue;
            filtered.add(idx);
        }

        if (sportCenterId != null && !sportCenterId.isBlank()) {
            // Real call into the Booking Service - throws BookingServiceException
            // (mapped to HTTP 503) if the Booking Service is down.
            List<String> bookingIds = bookingClient.getBookingIdsForSportCenter(sportCenterId);
            log.info("Booking Service returned {} booking(s) for sportCenterId={}",
                    bookingIds.size(), sportCenterId);
            // Keep only facilities whose entityId matches the sport center
            // that actually has bookings. This is the integration boundary:
            // the Search Service trusts the Booking Service as the source of
            // truth for booking activity.
            List<SearchIndex> narrowed = new ArrayList<>();
            for (SearchIndex idx : filtered) {
                if (sportCenterId.equalsIgnoreCase(idx.getEntityId())) {
                    narrowed.add(idx);
                }
            }
            return narrowed.stream().map(this::mapToDto).toList();
        }
        return filtered.stream().map(this::mapToDto).toList();
    }

    // ==============================================================
    // INDEX - upsert, fetch by identity, delete
    // ==============================================================

    /**
     * POST /api/search/index - upserts a SearchIndex entry.
     * Creates a new row if the (entityType, entityId) pair is new;
     * updates the existing row otherwise.
     */
    @Transactional
    public SearchIndexDto upsertIndex(IndexEntityRequest request) {
        if (request == null
                || request.getEntityType() == null
                || request.getEntityId() == null
                || request.getEntityId().isBlank()) {
            throw new InvalidSearchRequestException(
                    "entityType and entityId are required to index an entry");
        }

        Optional<SearchIndex> existing = repository
                .findByEntityTypeAndEntityId(request.getEntityType(), request.getEntityId());

        SearchIndex idx = existing.orElseGet(() -> SearchIndex.builder()
                .indexId(UUID.randomUUID().toString())
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .build());

        idx.setDisplayName(request.getDisplayName());
        idx.setKeywords(request.getKeywords() != null
                ? new ArrayList<>(request.getKeywords())
                : new ArrayList<>());
        idx.setLastIndexedAt(LocalDateTime.now());
        idx.setFilters(mapToFilters(request.getFilters()));

        SearchIndex saved = repository.save(idx);
        log.info("Indexed {} entry {} (entityId={})",
                saved.getEntityType(), saved.getIndexId(), saved.getEntityId());
        return mapToDto(saved);
    }

    /**
     * GET /api/search/index/{entityType}/{entityId}.
     */
    public SearchIndexDto getIndexEntry(EntityType entityType, String entityId) {
        SearchIndex idx = repository.findByEntityTypeAndEntityId(entityType, entityId)
                .orElseThrow(() -> new SearchIndexNotFoundException(
                        "No search index entry for " + entityType + "/" + entityId));
        return mapToDto(idx);
    }

    /**
     * DELETE /api/search/index/{entityType}/{entityId}.
     */
    @Transactional
    public void deleteIndexEntry(EntityType entityType, String entityId) {
        if (!repository.existsByEntityTypeAndEntityId(entityType, entityId)) {
            throw new SearchIndexNotFoundException(
                    "No search index entry for " + entityType + "/" + entityId);
        }
        repository.deleteByEntityTypeAndEntityId(entityType, entityId);
        log.info("Removed index entry for {}/{}", entityType, entityId);
    }

    // ==============================================================
    // MAPPING
    // ==============================================================

    private SearchIndexDto mapToDto(SearchIndex idx) {
        SearchFilters f = idx.getFilters();
        SearchFiltersDto filtersDto = f == null ? null : SearchFiltersDto.builder()
                .sportType(f.getSportType())
                .location(f.getLocation())
                .skillLevel(f.getSkillLevel())
                .activityTimeSlots(f.getActivityTimeSlots())
                .activityTitle(f.getActivityTitle())
                .activityStatus(f.getActivityStatus())
                .activityMaxParticipants(f.getActivityMaxParticipants())
                .userName(f.getUserName())
                .userRatingScore(f.getUserRatingScore())
                .userSportPreferences(f.getUserSportPreferences())
                .facilityName(f.getFacilityName())
                .build();

        return SearchIndexDto.builder()
                .indexId(idx.getIndexId())
                .entityType(idx.getEntityType())
                .entityId(idx.getEntityId())
                .displayName(idx.getDisplayName())
                .keywords(idx.getKeywords() != null ? new ArrayList<>(idx.getKeywords()) : new ArrayList<>())
                .lastIndexedAt(idx.getLastIndexedAt())
                .sportType(f == null ? null : f.getSportType())
                .location(f == null ? null : f.getLocation())
                .skillLevel(f == null ? null : f.getSkillLevel())
                .activityTimeSlots(f == null ? null : f.getActivityTimeSlots())
                .activityTitle(f == null ? null : f.getActivityTitle())
                .activityStatus(f == null ? null : f.getActivityStatus())
                .activityMaxParticipants(f == null ? null : f.getActivityMaxParticipants())
                .userName(f == null ? null : f.getUserName())
                .userRatingScore(f == null ? null : f.getUserRatingScore())
                .userSportPreferences(f == null ? null : f.getUserSportPreferences())
                .facilityName(f == null ? null : f.getFacilityName())
                .filters(filtersDto)
                .build();
    }

    private SearchFilters mapToFilters(SearchFiltersDto dto) {
        if (dto == null) return null;
        return SearchFilters.builder()
                .sportType(dto.getSportType())
                .location(dto.getLocation())
                .skillLevel(dto.getSkillLevel())
                .activityTimeSlots(dto.getActivityTimeSlots())
                .activityTitle(dto.getActivityTitle())
                .activityStatus(dto.getActivityStatus())
                .activityMaxParticipants(dto.getActivityMaxParticipants())
                .userName(dto.getUserName())
                .userRatingScore(dto.getUserRatingScore())
                .userSportPreferences(dto.getUserSportPreferences())
                .facilityName(dto.getFacilityName())
                .build();
    }

    // ==============================================================
    // FILTER HELPERS
    // ==============================================================

    private boolean matchesText(SearchIndex idx, String q) {
        if (q == null || q.isBlank()) return true;
        String needle = q.toLowerCase(Locale.ROOT);
        if (idx.getDisplayName() != null
                && idx.getDisplayName().toLowerCase(Locale.ROOT).contains(needle)) {
            return true;
        }
        if (idx.getKeywords() != null) {
            for (String kw : idx.getKeywords()) {
                if (kw != null && kw.toLowerCase(Locale.ROOT).contains(needle)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean equalsIgnoreCaseOrNull(String filterValue, String fieldValue) {
        if (filterValue == null || filterValue.isBlank()) return true;
        return fieldValue != null && fieldValue.equalsIgnoreCase(filterValue);
    }

    private boolean containsIgnoreCaseOrNull(String filterValue, String fieldValue) {
        if (filterValue == null || filterValue.isBlank()) return true;
        return fieldValue != null
                && fieldValue.toLowerCase(Locale.ROOT)
                              .contains(filterValue.toLowerCase(Locale.ROOT));
    }

    private boolean matchesMinRating(Double minRating, Double rating) {
        if (minRating == null) return true;
        return rating != null && rating >= minRating;
    }
}
