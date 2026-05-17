package com.sportlink.matchingservice.client;

import com.sportlink.matchingservice.dto.SearchActivityDto;
import com.sportlink.matchingservice.dto.SearchUserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.UUID;

/**
 * Client for Search Service.
 * Currently stubbed (sportlink.mock.search=true in application.properties).
 * Switch to false and set sportlink.services.search.url once Search service is running.
 */
@Component
@Slf4j
public class SearchServiceClient {

    private final WebClient webClient;
    private final boolean mock;

    public SearchServiceClient(
            WebClient.Builder webClientBuilder,
            @Value("${sportlink.services.search.url}") String baseUrl,
            @Value("${sportlink.mock.search:true}") boolean mock) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.mock = mock;
    }

    /**
     * Retrieve candidate users from the Search index, pre-filtered by sport type,
     * skill level, city, and district.
     * Corresponds to GET /api/search/users in the Search Service.
     */
    public List<SearchUserDto> findCandidateUsers(
            List<String> sportTypes,
            String skillLevel,
            String city,
            String district) {

        if (mock) {
            log.debug("Mock Search: returning stub user candidates for sport={}, skill={}, city={}",
                    sportTypes, skillLevel, city);
            return buildMockUserCandidates();
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search/users")
                        .queryParam("sportType", String.join(",", sportTypes))
                        .queryParam("skillLevel", skillLevel)
                        .queryParam("location", city)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<SearchUserDto>>() {})
                .block();
    }

    /**
     * Retrieve candidate activities from the Search index, pre-filtered by sport type,
     * city, and status (PLANNED/ACTIVE only).
     * Corresponds to GET /api/search/activities in the Search Service.
     */
    public List<SearchActivityDto> findCandidateActivities(
            List<String> sportTypes,
            String city) {

        if (mock) {
            log.debug("Mock Search: returning stub activity candidates for sport={}, city={}", sportTypes, city);
            return buildMockActivityCandidates();
        }

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/search/activities")
                        .queryParam("sportType", String.join(",", sportTypes))
                        .queryParam("location", city)
                        .queryParam("activityStatus", "PLANNED,ACTIVE")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<SearchActivityDto>>() {})
                .block();
    }

    // -------------------------------------------------------------------------
    // Stub data (removed once Search service is connected)
    // -------------------------------------------------------------------------

    private List<SearchUserDto> buildMockUserCandidates() {
        SearchUserDto u1 = new SearchUserDto();
        u1.setUserId(UUID.randomUUID());
        u1.setSkillLevel("INTERMEDIATE");
        u1.setCity("Tallinn");
        u1.setDistrict("Kesklinn");
        u1.setSportPreferences(List.of("badminton", "football"));
        u1.setUserRatingScore(4.2);

        SearchUserDto u2 = new SearchUserDto();
        u2.setUserId(UUID.randomUUID());
        u2.setSkillLevel("ADVANCED");
        u2.setCity("Tallinn");
        u2.setDistrict("Mustamäe");
        u2.setSportPreferences(List.of("badminton"));
        u2.setUserRatingScore(3.8);

        return List.of(u1, u2);
    }

    private List<SearchActivityDto> buildMockActivityCandidates() {
        SearchActivityDto a1 = new SearchActivityDto();
        a1.setActivityId(UUID.randomUUID());
        a1.setSportType("badminton");
        a1.setActivityStatus("PLANNED");
        a1.setCity("Tallinn");
        a1.setDistrict("Kesklinn");
        a1.setSkillLevel("INTERMEDIATE");
        a1.setMaxParticipants(4);

        SearchActivityDto a2 = new SearchActivityDto();
        a2.setActivityId(UUID.randomUUID());
        a2.setSportType("football");
        a2.setActivityStatus("ACTIVE");
        a2.setCity("Tallinn");
        a2.setDistrict("Lasnamäe");
        a2.setSkillLevel("BEGINNER");
        a2.setMaxParticipants(10);

        return List.of(a1, a2);
    }
}
