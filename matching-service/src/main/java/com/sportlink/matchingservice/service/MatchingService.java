package com.sportlink.matchingservice.service;

import com.sportlink.matchingservice.client.AccountManagementClient;
import com.sportlink.matchingservice.client.SchedulingServiceClient;
import com.sportlink.matchingservice.client.SearchServiceClient;
import com.sportlink.matchingservice.dto.*;
import com.sportlink.matchingservice.model.*;
import com.sportlink.matchingservice.repository.ActivitySuggestionRepository;
import com.sportlink.matchingservice.repository.UserSuggestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingService {

    private final UserSuggestionRepository userSuggestionRepository;
    private final ActivitySuggestionRepository activitySuggestionRepository;
    private final AccountManagementClient accountManagementClient;
    private final SearchServiceClient searchServiceClient;
    private final SchedulingServiceClient schedulingServiceClient;
    private final CompatibilityScorer scorer;

    // -------------------------------------------------------------------------
    // User suggestions
    // -------------------------------------------------------------------------

    /**
     * GET /api/matching/users/{userId}/suggestions
     * Returns the most recently cached suggestion list.
     */
    public UserSuggestion getCachedUserSuggestions(UUID userId, int limit, double minScore) {
        UserSuggestion cached = userSuggestionRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No suggestions found for user " + userId + ". Trigger POST to compute."));

        // Apply in-memory filtering before returning
        List<SuggestedUser> filtered = cached.getSuggestedUsers().stream()
                .filter(s -> s.getCompatibilityScore() >= minScore)
                .limit(limit)
                .toList();

        cached.setSuggestedUsers(filtered);
        return cached;
    }

    /**
     * POST /api/matching/users/{userId}/suggestions
     * Recomputes suggestions fresh: queries Search for candidates,
     * scores each one, persists a new UserSuggestion record, and returns it.
     */
    public UserSuggestion recomputeUserSuggestions(UUID userId, int limit, double minScore) {
        // 1. Validate requester exists in Account Management (real integration)
        UserProfileDto requester = accountManagementClient.getUserProfile(userId);
        if (requester == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        if (!"ACTIVE".equalsIgnoreCase(requester.getStatus())) {
            throw new IllegalStateException("User account is not active: " + userId);
        }

        // 2. Fetch candidate pool from Search Service (stubbed until Search is running)
        List<SearchUserDto> candidates = searchServiceClient.findCandidateUsers(
                requester.getSportPreferences(),
                requester.getSkillLevel(),
                requester.getCity(),
                requester.getDistrict()
        );

        // 3. Score each candidate
        List<SuggestedUser> scored = candidates.stream()
                .filter(c -> !c.getUserId().equals(userId)) // exclude self
                .map(candidate -> {
                    // Fetch full profile for scoring (language, detailed location, etc.)
                    UserProfileDto candidateProfile = accountManagementClient.getUserProfile(candidate.getUserId());
                    if (candidateProfile == null || !"ACTIVE".equalsIgnoreCase(candidateProfile.getStatus())) {
                        return null; // skip unavailable / suspended users
                    }

                    double scheduleOverlap = schedulingServiceClient.resolveScheduleOverlap(userId, candidate.getUserId());
                    double rating = candidate.getUserRatingScore();

                    return scorer.score(requester, candidateProfile, rating, scheduleOverlap);
                })
                .filter(s -> s != null && s.getCompatibilityScore() >= minScore)
                .sorted(Comparator.comparingDouble(SuggestedUser::getCompatibilityScore).reversed())
                .limit(limit)
                .toList();

        // 4. Persist the new snapshot
        UserSuggestion suggestion = UserSuggestion.builder()
                .userId(userId)
                .suggestedUsers(scored)
                .build();

        return userSuggestionRepository.save(suggestion);
    }

    // -------------------------------------------------------------------------
    // Activity suggestions
    // -------------------------------------------------------------------------

    /**
     * GET /api/matching/activities/{userId}/suggestions
     * Returns the most recently cached activity suggestion list.
     */
    public ActivitySuggestion getCachedActivitySuggestions(UUID userId, int limit, double minScore) {
        ActivitySuggestion cached = activitySuggestionRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new NoSuchElementException(
                        "No activity suggestions found for user " + userId + ". Trigger POST to compute."));

        List<SuggestedActivity> filtered = cached.getSuggestedActivities().stream()
                .filter(a -> a.getRelevanceScore() >= minScore)
                .limit(limit)
                .toList();

        cached.setSuggestedActivities(filtered);
        return cached;
    }

    /**
     * POST /api/matching/activities/{userId}/suggestions
     * Recomputes activity suggestions: queries Search for open activities,
     * ranks them by relevance, persists a new ActivitySuggestion record.
     */
    public ActivitySuggestion recomputeActivitySuggestions(UUID userId, int limit, double minScore) {
        // 1. Validate user
        UserProfileDto requester = accountManagementClient.getUserProfile(userId);
        if (requester == null) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        if (!"ACTIVE".equalsIgnoreCase(requester.getStatus())) {
            throw new IllegalStateException("User account is not active: " + userId);
        }

        // 2. Fetch candidate activities (stubbed until Search is running)
        List<SearchActivityDto> candidates = searchServiceClient.findCandidateActivities(
                requester.getSportPreferences(),
                requester.getCity()
        );

        // 3. Score each activity
        List<SuggestedActivity> scored = candidates.stream()
                .filter(a -> List.of("PLANNED", "ACTIVE").contains(a.getActivityStatus()))
                .map(activity -> scoreActivity(requester, activity))
                .filter(a -> a.getRelevanceScore() >= minScore)
                .sorted(Comparator.comparingDouble(SuggestedActivity::getRelevanceScore).reversed())
                .limit(limit)
                .toList();

        // 4. Persist
        ActivitySuggestion suggestion = ActivitySuggestion.builder()
                .userId(userId)
                .suggestedActivities(scored)
                .build();

        return activitySuggestionRepository.save(suggestion);
    }

    // -------------------------------------------------------------------------
    // On-demand compatibility (not persisted)
    // -------------------------------------------------------------------------

    /**
     * GET /api/matching/compatibility?userAId=...&userBId=...
     * Computes compatibility between two specific users without persisting anything.
     */
    public CompatibilityResponse computeCompatibility(UUID userAId, UUID userBId) {
        UserProfileDto userA = accountManagementClient.getUserProfile(userAId);
        if (userA == null) {
            throw new IllegalArgumentException("User not found: " + userAId);
        }
        UserProfileDto userB = accountManagementClient.getUserProfile(userBId);
        if (userB == null) {
            throw new IllegalArgumentException("User not found: " + userBId);
        }

        double scheduleOverlap = schedulingServiceClient.resolveScheduleOverlap(userAId, userBId);
        // For the on-demand call we use userB's labelValue as their rating proxy
        double ratingB = userB.getLabelValue();

        SuggestedUser result = scorer.score(userA, userB, ratingB, scheduleOverlap);

        return CompatibilityResponse.builder()
                .userAId(userAId)
                .userBId(userBId)
                .compatibilityScore(result.getCompatibilityScore())
                .sportScore(result.getSportScore())
                .skillScore(result.getSkillScore())
                .locationScore(result.getLocationScore())
                .ratingScore(result.getRatingScore())
                .languageScore(result.getLanguageScore())
                .build();
    }

    // -------------------------------------------------------------------------
    // Invalidation
    // -------------------------------------------------------------------------

    /**
     * DELETE /api/matching/suggestions/{suggestionId}
     * Deletes a stale suggestion record — called internally when upstream events
     * (UserProfileUpdated, RatingCreated, ActivityCancelled) make the cache obsolete.
     * Works for both user and activity suggestions by checking both tables.
     */
    public void invalidateSuggestion(UUID suggestionId) {
        if (userSuggestionRepository.existsById(suggestionId)) {
            userSuggestionRepository.deleteById(suggestionId);
            return;
        }
        if (activitySuggestionRepository.existsById(suggestionId)) {
            activitySuggestionRepository.deleteById(suggestionId);
            return;
        }
        throw new NoSuchElementException("Suggestion not found: " + suggestionId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private SuggestedActivity scoreActivity(UserProfileDto requester, SearchActivityDto activity) {
        // Sport match
        boolean sportMatch = requester.getSportPreferences() != null
                && requester.getSportPreferences().stream()
                        .anyMatch(s -> s.equalsIgnoreCase(activity.getSportType()));
        double sportScore = sportMatch ? 1.0 : 0.0;

        // Skill match — same logic as user scorer
        double skillScore = scorer.scoreSkill(requester.getSkillLevel(), activity.getSkillLevel());

        // Location match
        double locationScore = scorer.scoreLocation(
                requester.getCity(), requester.getDistrict(),
                activity.getCity(), activity.getDistrict());

        double relevance = (sportScore * 0.50) + (skillScore * 0.30) + (locationScore * 0.20);

        return SuggestedActivity.builder()
                .activityId(activity.getActivityId())
                .relevanceScore(Math.round(relevance * 1000.0) / 1000.0)
                .sportScore(sportScore)
                .skillScore(skillScore)
                .locationScore(locationScore)
                .build();
    }
}
