package com.sportlink.scheduling.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportlink.scheduling.model.TimeSlot;
import com.sportlink.scheduling.model.TimeSlotStatus;


public interface TimeSlotRepository extends JpaRepository<TimeSlot, UUID> {

    /**
     
     *   SELECT * FROM time_slots
     *   WHERE status = :status
     *     AND start_time >= :from
     *     AND end_time   <= :to
     */
    List<TimeSlot> findByStatusAndStartTimeGreaterThanEqualAndEndTimeLessThanEqual(
            TimeSlotStatus status, LocalDateTime from, LocalDateTime to);

    /**
     * Find slots reserved by a specific entity (an activity or a booking).
     */
    List<TimeSlot> findByReservedForId(UUID reservedForId);
}