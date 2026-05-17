package com.sportlink.matchingservice.client;

import com.sportlink.matchingservice.dto.UserProfileDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.UUID;

/**
 * Client for Account Management Service.
 * mock flag is false by default — this service is already running.
 */
@Component
@Slf4j
public class AccountManagementClient {

    private final WebClient webClient;
    private final boolean mock;

    public AccountManagementClient(
            WebClient.Builder webClientBuilder,
            @Value("${sportlink.services.account-management.url}") String baseUrl,
            @Value("${sportlink.mock.account-management:false}") boolean mock) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.mock = mock;
    }

    /**
     * Fetch the full user profile needed for compatibility scoring.
     * Returns null if the user is not found (404).
     * Throws RuntimeException on unexpected errors so the service layer can handle them.
     */
    public UserProfileDto getUserProfile(UUID userId) {
        if (mock) {
            log.debug("Mock mode: returning stub profile for user {}", userId);
            return buildMockProfile(userId);
        }

        try {
            return webClient.get()
                    .uri("/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserProfileDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("User {} not found in Account Management", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user profile for {}: {}", userId, e.getMessage());
            throw new RuntimeException("Account Management Service unavailable", e);
        }
    }

    /**
     * Lightweight existence check — used before running the full scoring pipeline.
     */
    public boolean userExists(UUID userId) {
        return getUserProfile(userId) != null;
    }

    // -------------------------------------------------------------------------
    // Mock helpers (only used when sportlink.mock.account-management=true)
    // -------------------------------------------------------------------------

    private UserProfileDto buildMockProfile(UUID userId) {
        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(userId);
        dto.setName("Mock User");
        dto.setSkillLevel("INTERMEDIATE");
        dto.setLanguage("English");
        dto.setSportPreferences(java.util.List.of("badminton", "football"));
        dto.setCity("Tallinn");
        dto.setDistrict("Kesklinn");
        dto.setBehaviorLabel("FAIR_PLAY");
        dto.setLabelValue(4.0);
        dto.setStatus("ACTIVE");
        return dto;
    }
}
