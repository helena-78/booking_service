package com.sportlink.activitymanagement.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.sportlink.activitymanagement.dto.UserDto;
import com.sportlink.activitymanagement.exception.UserNotValidException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AccountManagementClient {

    private final WebClient.Builder webClientBuilder;
    private final boolean mock;
    private final String accountServiceUrl;

    public AccountManagementClient(WebClient.Builder webClientBuilder,
                                   @Value("${sportlink.mock.dependencies:true}") boolean mock,
                                   @Value("${sportlink.services.account-management.url:http://localhost:8081}") String accountServiceUrl) {
        this.webClientBuilder = webClientBuilder;
        this.mock = mock;
        this.accountServiceUrl = accountServiceUrl;
    }

    
    public UserDto getUser(UUID userId) {
        if (mock) {
            log.debug("Mock mode: returning stub user for {}", userId);
            return UserDto.builder()
                    .userId(userId)
                    .name("Mock User " + userId.toString().substring(0, 8))
                    .status("ACTIVE")
                    .role("USER")
                    .build();
        }

        try {
            return webClientBuilder.build()
                    .get()
                    .uri(accountServiceUrl + "/api/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            throw new UserNotValidException("User " + userId + " not found");
        } catch (Exception e) {
            log.error("Failed to call Account Management for user {}: {}", userId, e.getMessage());
            throw new UserNotValidException("Could not validate user " + userId
                    + ": " + e.getMessage());
        }
    }

    
    public UserDto validateActiveUser(UUID userId) {
        UserDto user = getUser(userId);
        if (user == null) {
            throw new UserNotValidException("User " + userId + " not found");
        }
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new UserNotValidException("User " + userId + " is not active (status="
                    + user.getStatus() + ")");
        }
        return user;
    }
}