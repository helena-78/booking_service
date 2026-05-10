package com.sportlink.scheduling.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Server generates slotId and sets initial status = RESERVED.
 *
 * organizerId references the user who owns the booking; reservedForId
 * references the activity or booking the slot is held for.
 * Both are logical references to other services' entities.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveTimeSlotRequest {

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @NotNull(message = "organizerId is required")
    private UUID organizerId;

    @NotNull(message = "reservedForId is required")
    private UUID reservedForId;
}