package com.sportlink.matchingservice.controller;

import com.sportlink.matchingservice.dto.ActivitySuggestionResponse;
import com.sportlink.matchingservice.dto.CompatibilityResponse;
import com.sportlink.matchingservice.dto.UserSuggestionResponse;
import com.sportlink.matchingservice.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Compute and retrieve user and activity suggestions")
public class MatchingController {

    private final MatchingService matchingService;

    // -------------------------------------------------------------------------
    // User suggestions
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Get cached user suggestions",
        description = "Returns the most recently computed suggestion list for the given user. " +
                      "Use POST to trigger a fresh recomputation."
    )
    @GetMapping("/users/{userId}/suggestions")
    public ResponseEntity<UserSuggestionResponse> getCachedUserSuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0.0") double minScore) {
        return ResponseEntity.ok(
                matchingService.getCachedUserSuggestions(userId, limit, minScore).toResponse());
    }

    @Operation(
        summary = "Recompute user suggestions",
        description = "Queries Search Service for candidate users, applies compatibility scoring " +
                      "(sport, skill, location, rating, language, schedule), persists and returns " +
                      "a fresh UserSuggestion."
    )
    @PostMapping("/users/{userId}/suggestions")
    public ResponseEntity<UserSuggestionResponse> recomputeUserSuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0.0") double minScore) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                matchingService.recomputeUserSuggestions(userId, limit, minScore).toResponse());
    }

    // -------------------------------------------------------------------------
    // Activity suggestions
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Get cached activity suggestions",
        description = "Returns the most recently computed activity suggestion list for the given user."
    )
    @GetMapping("/activities/{userId}/suggestions")
    public ResponseEntity<ActivitySuggestionResponse> getCachedActivitySuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0.0") double minScore) {
        return ResponseEntity.ok(
                matchingService.getCachedActivitySuggestions(userId, limit, minScore).toResponse());
    }

    @Operation(
        summary = "Recompute activity suggestions",
        description = "Queries Search Service for open activities matching the user's sport preferences " +
                      "and location, ranks them by relevance, persists and returns a fresh ActivitySuggestion."
    )
    @PostMapping("/activities/{userId}/suggestions")
    public ResponseEntity<ActivitySuggestionResponse> recomputeActivitySuggestions(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(defaultValue = "0.0") double minScore) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                matchingService.recomputeActivitySuggestions(userId, limit, minScore).toResponse());
    }

    // -------------------------------------------------------------------------
    // On-demand compatibility
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Compute compatibility between two users",
        description = "Calculates a compatibility score for a specific user pair without persisting anything. " +
                      "Returns the total score and all component breakdowns. " +
                      "Used by Activity Management when an organizer inspects a candidate player."
    )
    @GetMapping("/compatibility")
    public ResponseEntity<CompatibilityResponse> computeCompatibility(
            @RequestParam UUID userAId,
            @RequestParam UUID userBId) {
        return ResponseEntity.ok(matchingService.computeCompatibility(userAId, userBId));
    }

    // -------------------------------------------------------------------------
    // Invalidation
    // -------------------------------------------------------------------------

    @Operation(
        summary = "Invalidate a stale suggestion",
        description = "Deletes a suggestion record by ID. Called internally when upstream events " +
                      "(UserProfileUpdated, RatingCreated, ActivityCancelled) make the cached suggestion obsolete. " +
                      "Works for both user and activity suggestions."
    )
    @DeleteMapping("/suggestions/{suggestionId}")
    public ResponseEntity<Void> invalidateSuggestion(@PathVariable UUID suggestionId) {
        matchingService.invalidateSuggestion(suggestionId);
        return ResponseEntity.noContent().build();
    }
}
