package com.sportlink.matchingservice.repository;

import com.sportlink.matchingservice.model.UserSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSuggestionRepository extends JpaRepository<UserSuggestion, UUID> {

    /**
     * Fetch the most recently created suggestion list for the given user.
     * Used by GET /api/matching/users/{userId}/suggestions.
     */
    Optional<UserSuggestion> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
