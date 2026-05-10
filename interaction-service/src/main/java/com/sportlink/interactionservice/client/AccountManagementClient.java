package com.sportlink.interactionservice.client;

import com.sportlink.interactionservice.dto.response.UserSummaryResponse;
import com.sportlink.interactionservice.exception.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountManagementClient {

    private final WebClient.Builder webClientBuilder;

    /**
     * Fetches minimal user identity from Account Management Service.
     * Protected by a circuit breaker — when Account Management is unavailable the
     * fallback is invoked, allowing the interaction service to continue operating.
     */
    @CircuitBreaker(name = "account-management", fallbackMethod = "getUserFallback")
    public UserSummaryResponse getUserById(UUID userId) {
        return webClientBuilder.build()
                .get()
                .uri("http://account-management-service/api/users/{id}", userId)
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.NOT_FOUND,
                        response -> Mono.error(new ResourceNotFoundException("User not found: " + userId))
                )
                .bodyToMono(UserSummaryResponse.class)
                .block();
    }

    // Fallback: Account Management is down — return a stub so the service stays operational
    public UserSummaryResponse getUserFallback(UUID userId, Exception ex) {
        log.warn("Account Management Service unavailable for user {}. Reason: {}", userId, ex.getMessage());
        return UserSummaryResponse.builder()
                .userId(userId)
                .username("unknown")
                .build();
    }
}
