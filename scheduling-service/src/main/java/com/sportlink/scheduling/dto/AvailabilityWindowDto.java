package com.sportlink.scheduling.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sportlink.scheduling.model.DayOfWeek;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for an availability window — used for both reading  and
 * writing a user's recurring availability.
 *
 * windowId is null when the client creates a new window.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AvailabilityWindowDto {

    private UUID windowId;

    @NotNull(message = "timeRangeFrom is required")
    private LocalDateTime timeRangeFrom;

    @NotNull(message = "timeRangeTo is required")
    private LocalDateTime timeRangeTo;

    @NotNull(message = "dayOfWeek is required")
    private DayOfWeek dayOfWeek;
}