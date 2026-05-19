package com.sportlink.moderation.client;

import com.sportlink.moderation.client.dto.UpdateUserStatusBody;
import com.sportlink.moderation.exception.AccountSyncException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountManagementClient {

    private static final String CB_INSTANCE = "accountManagement";

    private final WebClient accountManagementWebClient;

    @CircuitBreaker(name = CB_INSTANCE, fallbackMethod = "updateUserStatusFallback")
    @Retry(name = CB_INSTANCE)
    public void updateUserStatus(UUID userId, String status) {
        try {
            WebClient.RequestBodySpec spec = accountManagementWebClient
                    .put()
                    .uri("/api/users/{id}/status", userId);

            String authHeader = currentAuthHeader();
            if (authHeader != null) {
                spec = spec.header(HttpHeaders.AUTHORIZATION, authHeader);
            }

            spec
                    .bodyValue(UpdateUserStatusBody.builder().status(status).build())
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(3))
                    .block();
            log.info("Account Management sync OK for user {} -> {}", userId, status);
        } catch (WebClientResponseException ex) {
            log.warn("Account Management returned {} for user {}: {}",
                    ex.getStatusCode(), userId, ex.getResponseBodyAsString());
            throw new AccountSyncException(
                    "Account Management rejected status update: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.warn("Account Management call failed for user {}: {}", userId, ex.getMessage());
            throw new AccountSyncException(
                    "Account Management is unreachable: " + ex.getMessage(), ex);
        }
    }

    private String currentAuthHeader() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
    }

    @SuppressWarnings("unused")
    private void updateUserStatusFallback(UUID userId, String status, Throwable ex) {
        log.error("Resilience4j fallback triggered for user {} (status={}): {}",
                userId, status, ex.toString());
        throw new AccountSyncException(
                "Account Management is unavailable after retries and circuit-breaker fallback: "
                        + ex.getMessage(), ex);
    }
}
