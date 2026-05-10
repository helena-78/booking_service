package com.sportlink.ratingservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sportlink.ratingservice.dto.RatingRequest;
import com.sportlink.ratingservice.model.Rating;
import com.sportlink.ratingservice.model.UserReputation;
import com.sportlink.ratingservice.repository.RatingRepository;
import com.sportlink.ratingservice.repository.UserReputationRepository;
import com.sportlink.ratingservice.stub.AccountManagementStub;
import com.sportlink.ratingservice.stub.ActivityManagementStub;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserReputationRepository reputationRepository;
    private final AccountManagementStub accountManagementStub;
    private final ActivityManagementStub activityManagementStub;

    public Rating submitRating(RatingRequest request) {

        // Validate both users exist (stub)
        if (!accountManagementStub.userExists(request.getReviewerId())) {
            throw new IllegalArgumentException("Reviewer not found: " + request.getReviewerId());
        }
        if (!accountManagementStub.userExists(request.getRevieweeId())) {
            throw new IllegalArgumentException("Reviewee not found: " + request.getRevieweeId());
        }

        // Validate shared participation and activity is completed (stub)
        if (!activityManagementStub.bothParticipatedAndActivityCompleted(
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

    public List<Rating> getRatings(UUID reviewerId, UUID revieweeId, UUID activityId) {
        if (reviewerId != null) return ratingRepository.findByReviewerId(reviewerId);
        if (revieweeId != null) return ratingRepository.findByRevieweeId(revieweeId);
        if (activityId != null) return ratingRepository.findByActivityId(activityId);
        return ratingRepository.findAll();
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

        return reputationRepository.save(reputation);
    }
}
