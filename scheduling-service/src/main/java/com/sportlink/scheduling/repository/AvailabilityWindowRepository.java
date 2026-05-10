package com.sportlink.scheduling.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportlink.scheduling.model.AvailabilityWindow;
import com.sportlink.scheduling.model.DayOfWeek;

/**
 * JPA repository for AvailabilityWindow.
 *
 * Custom finders for fetching a user's recurring availability,
 * used by:
 *   GET /api/timeslots/users/{userId}/availability
 *   POST /api/timeslots/resolve  (to compute overlapping availability)
 */
public interface AvailabilityWindowRepository extends JpaRepository<AvailabilityWindow, UUID> {

    /**
     * All availability windows of a single user.
     */
    List<AvailabilityWindow> findByUserId(UUID userId);

    /**
     * Availability windows of a user on a specific day of the week.
     * Used for finer-grained queries.
     */
    List<AvailabilityWindow> findByUserIdAndDayOfWeek(UUID userId, DayOfWeek dayOfWeek);

    /**
     * Delete all availability windows of a user — used when a user updates
     * (replaces) their entire availability schedule via PUT.
     */
    void deleteByUserId(UUID userId);
}