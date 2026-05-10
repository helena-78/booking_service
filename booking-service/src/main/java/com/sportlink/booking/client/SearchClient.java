package com.sportlink.booking.client;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.sportlink.booking.dto.FacilityDto;
import com.sportlink.booking.exception.SearchServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Synchronous client for the Search Service. 
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SearchClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.search.url:http://localhost:8086}")
    private String searchServiceUrl;

    /**
     * Search bookable facilities matching the optional filters.
     */
    public List<FacilityDto> searchFacilities(String sportType, String location, String timeSlotId) {
        StringBuilder url = new StringBuilder(searchServiceUrl)
                .append("/api/v1/search?entityType=FACILITY");
        if (sportType != null && !sportType.isBlank()) {
            url.append("&sportType=").append(sportType);
        }
        if (location != null && !location.isBlank()) {
            url.append("&location=").append(location);
        }
        if (timeSlotId != null && !timeSlotId.isBlank()) {
            url.append("&timeSlotId=").append(timeSlotId);
        }

        try {
            List<FacilityDto> facilities = webClientBuilder
                    .build()
                    .get()
                    .uri(url.toString())
                    .retrieve()
                    .bodyToFlux(FacilityDto.class)
                    .collectList()
                    .block();
            return facilities != null ? facilities : new ArrayList<>();
        } catch (Exception e) {
            log.error("Facility search via Search Service failed: {}", e.getMessage());
            throw new SearchServiceException("Search Service unavailable", e);
        }
    }
}
