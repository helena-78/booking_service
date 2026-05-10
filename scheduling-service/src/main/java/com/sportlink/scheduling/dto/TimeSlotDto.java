package com.sportlink.scheduling.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sportlink.scheduling.model.TimeSlotStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for time slot endpoints.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotDto {

    private UUID slotId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private TimeSlotStatus status;
    private UUID organizerId;
    private UUID reservedForId;
}