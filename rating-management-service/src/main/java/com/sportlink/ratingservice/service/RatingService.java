package com.sportlink.ratingservice.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.sportlink.ratingservice.dto.RatingRequest;
import com.sportlink.ratingservice.dto.RatingResponse;
import com.sportlink.ratingservice.dto.ReputationUpdatedEvent;
import com.sportlink.ratingservice.model.Rating;
import com.sportlink.ratingservice.model.UserReputation;
import com.sportlink.ratingservice.repository.RatingRepository;
import com.sportlink.ratingservice.repository.UserReputationRepository;
import com.sportlink.ratingservice.client.AccountManagementClient;
import com.sportlink.ratingservice.client.ActivityManagementClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service    
@RequiredArgsConstructor
public class RatingService {

    private final KafkaTemplate<String, ReputationUpdatedEvent> kafkaTemplate;
    private final RatingRepository ratingRepository;
    private final UserReputationRepository reputationRepository;
    private final AccountManagementClient accountManagementClient;
    private final ActivityManagementClient activityManagementClient;

    public Rating submitRating(RatingRequest request) {

        // Validate both users exist
        if (!accountManagementClient.userExists(request.getReviewerId())) {
            throw new IllegalArgumentException("Reviewer not found: " + request.getReviewerId());
        }
        if (!accountManagementClient.userExists(request.getRevieweeId())) {
            throw new IllegalArgumentException("Reviewee not found: " + request.getRevieweeId());
        }

        // Validate shared participation and activity is completed
        if (!activityManagementClient.bothParticipatedAndActivityCompleted(
                request.getActivityId(), request.getReviewerId(), request.getRevieweeId())) {
            throw new IllegalStateException("Both users must have participated in a COMPLETED activity.");
        }

        // Duplicate check
        if (ratingRepository.existsByReviewerIdAndRevieweeIdAndActivityId(
                request.getReviewerId(), request.getRevieweeId(), request.getActivityId())) {
            throw new IllegalStateException("Rating already submitted for this activity.");
        }

        Rating rating = Rating.builder()
                .reviewerId(request.getReviewerId())
                .revieweeId(request.getRevieweeId())
                .activityId(request.getActivityId())
                .behaviorValue(request.getBehaviorValue())
                .behaviorLabel(request.getBehaviorLabel())
                .skillValue(request.getSkillValue())
                .skillLabel(request.getSkillLabel())
                .build();

        Rating saved = ratingRepository.save(rating);
        recomputeReputation(request.getRevieweeId());
        return saved;
    }
    
    public Rating getRatingById(UUID ratingId) {
        return ratingRepository.findById(ratingId)
                .orElseThrow(() -> new NoSuchElementException("Rating not found: " + ratingId));
    }

    public RatingResponse getRatingResponseById(UUID ratingId) {
        return toResponse(getRatingById(ratingId), new HashMap<>());
    }

    public List<Rating> getRatings(UUID reviewerId, UUID revieweeId, UUID activityId) {
        if (reviewerId != null) return ratingRepository.findByReviewerId(reviewerId);
        if (revieweeId != null) return ratingRepository.findByRevieweeId(revieweeId);
        if (activityId != null) return ratingRepository.findByActivityId(activityId);
        return ratingRepository.findAll();
    }

    public List<RatingResponse> getRatingsResponse(UUID reviewerId, UUID revieweeId, UUID activityId) {
        Map<UUID, String> userNameCache = new HashMap<>();
        return getRatings(reviewerId, revieweeId, activityId).stream()
                .map(rating -> toResponse(rating, userNameCache))
                .toList();
    }

    public void deleteRating(UUID ratingId) {
        Rating rating = getRatingById(ratingId);
        ratingRepository.delete(rating);
        recomputeReputation(rating.getRevieweeId());
    }

    public UserReputation getReputation(UUID userId) {
        return reputationRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Reputation not found for user: " + userId));
    }

    public UserReputation recomputeReputation(UUID userId) {
        List<Rating> ratings = ratingRepository.findAllByRevieweeId(userId);

        double avg = ratings.stream()
                .mapToInt(r -> (r.getBehaviorValue() + r.getSkillValue()) / 2)
                .average()
                .orElse(0.0);

        String label = avg >= 4.5 ? "EXCELLENT" : avg >= 3.5 ? "GOOD" : avg >= 2.5 ? "AVERAGE" : "POOR";

        UserReputation reputation = reputationRepository.findById(userId)
                .orElse(UserReputation.builder().userId(userId).build());

        reputation.setAverageScore(avg);
        reputation.setUserLabel(label);
        reputation.setTotalRatings(ratings.size());
        reputation.setLastUpdatedAt(LocalDateTime.now());

        ReputationUpdatedEvent event = new ReputationUpdatedEvent(
                reputation.getUserId(),
                reputation.getAverageScore(),
                reputation.getUserLabel(),
                reputation.getTotalRatings()
        );
        
        try {
            kafkaTemplate.send("reputation-updated", userId.toString(), event);
        } catch (Exception e) {
            log.warn("Failed to send reputation-updated event to Kafka for user {}: {}", userId, e.getMessage());
            // Don't fail the request if Kafka is unavailable
        }
        
        return reputationRepository.save(reputation);
    }

    public RatingResponse toResponse(Rating rating, Map<UUID, String> userNameCache) {
        return RatingResponse.builder()
                .ratingId(rating.getRatingId())
                .reviewerId(rating.getReviewerId())
                .reviewerName(resolveUserName(rating.getReviewerId(), userNameCache))
                .revieweeId(rating.getRevieweeId())
                .revieweeName(resolveUserName(rating.getRevieweeId(), userNameCache))
                .activityId(rating.getActivityId())
                .behaviorValue(rating.getBehaviorValue())
                .behaviorLabel(rating.getBehaviorLabel())
                .skillValue(rating.getSkillValue())
                .skillLabel(rating.getSkillLabel())
                .createdAt(rating.getCreatedAt())
                .build();
    }

    private String resolveUserName(UUID userId, Map<UUID, String> userNameCache) {
        return userNameCache.computeIfAbsent(userId, accountManagementClient::getUserName);
    }
}
