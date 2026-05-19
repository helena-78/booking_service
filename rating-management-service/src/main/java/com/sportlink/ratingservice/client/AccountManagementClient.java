package com.sportlink.ratingservice.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountManagementClient {

    private final WebClient.Builder webClientBuilder;
    private final boolean mock;
    private final String accountManagementUrl;

    public AccountManagementClient(WebClient.Builder webClientBuilder,
                                   @Value("${sportlink.mock.dependencies:true}") boolean mock,
                                   @Value("${sportlink.services.account-management.url:http://account-service:8081}") String accountManagementUrl) {
        this.webClientBuilder = webClientBuilder;
        this.mock = mock;
        this.accountManagementUrl = accountManagementUrl;
    }

    public boolean userExists(UUID userId) {
        if (mock) {
            log.debug("Mock mode: assuming user {} exists", userId);
            return true;
        }

        try {
            webClientBuilder.build()
                    .get()
                    .uri(accountManagementUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (WebClientResponseException.NotFound e) {
            return false;
        } catch (Exception e) {
            log.error("Failed to check if user {} exists: {}", userId, e.getMessage());
            return false;
        }
    }

    public String getUserName(UUID userId) {
        if (mock) {
            log.debug("Mock mode: returning placeholder name for user {}", userId);
            return "Mock User";
        }

        try {
            UserProfile profile = webClientBuilder.build()
                    .get()
                    .uri(accountManagementUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserProfile.class)
                    .block();
            return profile != null && profile.getName() != null ? profile.getName() : "Unknown user";
        } catch (WebClientResponseException.NotFound e) {
            return "Unknown user";
        } catch (Exception e) {
            log.error("Failed to load user {} profile: {}", userId, e.getMessage());
            return "Unknown user";
        }
    }

    @Data
    private static class UserProfile {
        private UUID userId;
        private String name;
    }
}