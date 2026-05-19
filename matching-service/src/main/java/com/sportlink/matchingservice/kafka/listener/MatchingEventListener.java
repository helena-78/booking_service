package com.sportlink.matchingservice.kafka.listener;

import com.sportlink.matchingservice.kafka.event.*;
import com.sportlink.matchingservice.repository.ActivitySuggestionRepository;
import com.sportlink.matchingservice.repository.UserSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingEventListener {

    private final UserSuggestionRepository userSuggestionRepository;
    private final ActivitySuggestionRepository activitySuggestionRepository;

    /**
     * Listens for user profile updates and invalidates cached user suggestions for that user.
     * Triggers re-computation of suggestions on next request.
     */
    @KafkaListener(topics = "account-events", groupId = "matching-service")
    public void handleUserUpdated(UserUpdatedEvent event) {
        try {
            log.info("Received UserUpdatedEvent for user {}", event.getUserId());
            userSuggestionRepository.deleteByUserId(event.getUserId());
            log.info("Invalidated cached user suggestions for user {}", event.getUserId());
        } catch (Exception e) {
            log.warn("Failed to handle UserUpdatedEvent for user {}: {}", event.getUserId(), e.getMessage());
        }
    }

    /**
     * Listens for activity creation and invalidates activity suggestions for all users.
     * This ensures fresh suggestions when new activities become available.
     */
    @KafkaListener(topics = "activity-events", groupId = "matching-service")
    public void handleActivityCreated(ActivityCreatedEvent event) {
        try {
            log.info("Received ActivityCreatedEvent for activity {}", event.getActivityId());
            activitySuggestionRepository.deleteAll();
            log.info("Invalidated all cached activity suggestions due to new activity");
        } catch (Exception e) {
            log.warn("Failed to handle ActivityCreatedEvent: {}", e.getMessage());
        }
    }

    /**
     * Listens for activity cancellations and invalidates activity suggestions for all users.
     * This ensures cancelled activities don't appear in suggestions.
     */
    @KafkaListener(topics = "activity-events", groupId = "matching-service")
    public void handleActivityCancelled(ActivityCancelledEvent event) {
        try {
            log.info("Received ActivityCancelledEvent for activity {}", event.getActivityId());
            activitySuggestionRepository.deleteAll();
            log.info("Invalidated all cached activity suggestions due to activity cancellation");
        } catch (Exception e) {
            log.warn("Failed to handle ActivityCancelledEvent: {}", e.getMessage());
        }
    }

    /**
     * Listens for new ratings and invalidates suggestions for both reviewer and reviewee.
     * New ratings affect the compatibility scores in suggestions.
     */
    @KafkaListener(topics = "rating-events", groupId = "matching-service")
    public void handleRatingCreated(RatingCreatedEvent event) {
        try {
            log.info("Received RatingCreatedEvent for activity {}", event.getActivityId());
            userSuggestionRepository.deleteByUserId(event.getRevieweeId());
            userSuggestionRepository.deleteByUserId(event.getReviewerId());
            log.info("Invalidated cached user suggestions for reviewer {} and reviewee {}", 
                    event.getReviewerId(), event.getRevieweeId());
        } catch (Exception e) {
            log.warn("Failed to handle RatingCreatedEvent: {}", e.getMessage());
        }
    }

    /**
     * Listens for deleted ratings and invalidates suggestions for the reviewee.
     * Deleted ratings affect the reputation score used in matching.
     */
    @KafkaListener(topics = "rating-events", groupId = "matching-service")
    public void handleRatingDeleted(RatingDeletedEvent event) {
        try {
            log.info("Received RatingDeletedEvent for activity {}", event.getActivityId());
            userSuggestionRepository.deleteByUserId(event.getRevieweeId());
            log.info("Invalidated cached user suggestions for reviewee {}", event.getRevieweeId());
        } catch (Exception e) {
            log.warn("Failed to handle RatingDeletedEvent: {}", e.getMessage());
        }
    }

    /**
     * Listens for reputation updates and invalidates suggestions for that user.
     * Changes in reputation directly affect matching compatibility scores.
     */
    @KafkaListener(topics = "reputation-events", groupId = "matching-service")
    public void handleUserReputationUpdated(UserReputationUpdatedEvent event) {
        try {
            log.info("Received UserReputationUpdatedEvent for user {}", event.getUserId());
            userSuggestionRepository.deleteByUserId(event.getUserId());
            log.info("Invalidated cached user suggestions for user {} due to reputation update", event.getUserId());
        } catch (Exception e) {
            log.warn("Failed to handle UserReputationUpdatedEvent for user {}: {}", event.getUserId(), e.getMessage());
        }
    }
}
