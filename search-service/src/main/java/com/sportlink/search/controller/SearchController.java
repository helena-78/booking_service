package com.sportlink.search.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.search.dto.IndexEntityRequest;
import com.sportlink.search.dto.SearchIndexDto;
import com.sportlink.search.model.EntityType;
import com.sportlink.search.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public List<SearchIndexDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String activityStatus,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false) String timeSlotId) {

        // When the Booking Service calls /api/search?entityType=FACILITY&timeSlotId=...
        // (via its SearchClient when an organizer is picking a venue), forward
        // to the dedicated booking-aware facility search. Other callers can use
        // the same endpoint without a timeSlotId for plain cross-entity search.
        if (entityType == EntityType.FACILITY
                && timeSlotId != null && !timeSlotId.isBlank()) {
            return searchService.searchFacilitiesForBooking(sportType, location, timeSlotId);
        }
        return searchService.search(q, entityType, sportType, location,
                skillLevel, activityStatus, minRating, facilityName);
    }
    
    @GetMapping("/search/users")
    public List<SearchIndexDto> searchUsers(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String skillLevel,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Double userRatingScore,
            @RequestParam(required = false) String userSportPreferences) {
        return searchService.searchUsers(userName, sportType, skillLevel,
                location, userRatingScore, userSportPreferences);
    }
    
    @GetMapping("/search/activities")
    public List<SearchIndexDto> searchActivities(
            @RequestParam(required = false) String activityTitle,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String activityStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer activityMaxParticipants,
            @RequestParam(required = false) String activityTimeSlots) {
        return searchService.searchActivities(activityTitle, sportType,
                activityStatus, location, activityMaxParticipants, activityTimeSlots);
    }

    @GetMapping("/search/facilities")
    public List<SearchIndexDto> searchFacilities(
            @RequestParam(required = false) String facilityName,
            @RequestParam(required = false) String sportType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String sportCenterId) {
        return searchService.searchFacilities(facilityName, sportType, location, sportCenterId);
    }
    
    @PostMapping("/search/index")
    @ResponseStatus(HttpStatus.CREATED)
    public SearchIndexDto indexEntity(@RequestBody IndexEntityRequest request) {
        return searchService.upsertIndex(request);
    }

    @GetMapping("/search/index/{entityType}/{entityId}")
    public SearchIndexDto getIndexEntry(
            @PathVariable EntityType entityType,
            @PathVariable String entityId) {
        return searchService.getIndexEntry(entityType, entityId);
    }

    @DeleteMapping("/search/index/{entityType}/{entityId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIndexEntry(
            @PathVariable EntityType entityType,
            @PathVariable String entityId) {
        searchService.deleteIndexEntry(entityType, entityId);
    }
}
