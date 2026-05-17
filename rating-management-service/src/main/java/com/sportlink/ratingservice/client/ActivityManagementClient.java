package com.sportlink.ratingservice.client;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ActivityManagementClient {

    private final WebClient.Builder webClientBuilder;
    private final boolean mock;

    public ActivityManagementClient(WebClient.Builder webClientBuilder,
                                    @Value("${sportlink.mock.dependencies:true}") boolean mock) {
        this.webClientBuilder = webClientBuilder;
        this.mock = mock;
    }

    public boolean bothParticipatedAndActivityCompleted(UUID activityId, UUID reviewerId, UUID revieweeId) {
        if (mock) {
            log.debug("Mock mode: assuming activity {} is completed and both users {} and {} participated", activityId, reviewerId, revieweeId);
            return true;
        }

        try {
            ActivityDto activity = webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8083/api/activities/{id}", activityId) // change to docker service name
                    .retrieve()
                    .bodyToMono(ActivityDto.class)
                    .block();

            if (activity == null || !"COMPLETED".equals(activity.getStatus())) {
                return false;
            }

            List<ParticipantDto> participants = activity.getParticipants();
            if (participants == null) {
                return false;
            }

            boolean reviewerParticipated = participants.stream().anyMatch(p -> reviewerId.equals(p.getUserId()));
            boolean revieweeParticipated = participants.stream().anyMatch(p -> revieweeId.equals(p.getUserId()));

            return reviewerParticipated && revieweeParticipated;
        } catch (Exception e) {
            log.error("Failed to check activity {} participation: {}", activityId, e.getMessage());
            return false;
        }
    }

    // Local DTOs for the client
    public static class ActivityDto {
        private String status;
        private List<ParticipantDto> participants;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public List<ParticipantDto> getParticipants() { return participants; }
        public void setParticipants(List<ParticipantDto> participants) { this.participants = participants; }
    }

    public static class ParticipantDto {
        private UUID userId;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
    }
}