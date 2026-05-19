package com.sportlink.ratingservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportlink.ratingservice.dto.RatingRequest;
import com.sportlink.ratingservice.dto.RatingResponse;
import com.sportlink.ratingservice.dto.UserReputationResponse;
import com.sportlink.ratingservice.service.RatingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = {
    "http://localhost:8080",
    "http://frontend:8080"
})
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Rating Management", description = "Submit and manage post-activity ratings and reputations")
public class RatingController {

    private final RatingService ratingService;

    @Operation(summary = "Submit a new rating", description = "Validates participation via ActivityManagement stub, prevents duplicates")
    @PostMapping("/ratings")
    public ResponseEntity<RatingResponse> submitRating(@RequestBody RatingRequest request) {
        var savedRating = ratingService.submitRating(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ratingService.getRatingResponseById(savedRating.getRatingId()));
    }

    @Operation(summary = "Get a rating by ID")
    @GetMapping("/ratings/{ratingId}")
    public ResponseEntity<RatingResponse> getRating(@PathVariable UUID ratingId) {
        return ResponseEntity.ok(ratingService.getRatingResponseById(ratingId));
    }

    @Operation(summary = "List ratings", description = "Filter by reviewerId, revieweeId, or activityId")
    @GetMapping("/ratings")
    public ResponseEntity<List<RatingResponse>> listRatings(
            @RequestParam(required = false) UUID reviewerId,
            @RequestParam(required = false) UUID revieweeId,
            @RequestParam(required = false) UUID activityId) {
        return ResponseEntity.ok(ratingService.getRatingsResponse(reviewerId, revieweeId, activityId));
    }

    @Operation(summary = "Delete a rating")
    @DeleteMapping("/ratings/{ratingId}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID ratingId) {
        ratingService.deleteRating(ratingId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get aggregated reputation for a user")
    @GetMapping("/reputations/{userId}")
    public ResponseEntity<UserReputationResponse> getReputation(@PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.getReputation(userId).toResponse());
    }

    @Operation(summary = "Recompute reputation from all ratings")
    @PostMapping("/reputations/{userId}/recompute")
    public ResponseEntity<UserReputationResponse> recompute(@PathVariable UUID userId) {
        return ResponseEntity.ok(ratingService.recomputeReputation(userId).toResponse());
    }
}