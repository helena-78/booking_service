package com.sportlink.matchingservice.repository;

import com.sportlink.matchingservice.model.ActivitySuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ActivitySuggestionRepository extends JpaRepository<ActivitySuggestion, UUID> {

    /**
     * Fetch the most recently created activity suggestion list for the given user.
     * Used by GET /api/matching/activities/{userId}/suggestions.
     */
    Optional<ActivitySuggestion> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
