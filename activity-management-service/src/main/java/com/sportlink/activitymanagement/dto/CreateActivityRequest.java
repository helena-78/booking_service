package com.sportlink.activitymanagement.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateActivityRequest {

    @NotNull(message = "organizerId is required")
    private UUID organizerId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "sportType is required")
    private String sportType;

    @NotNull(message = "maxParticipants is required")
    @Min(value = 1, message = "maxParticipants must be at least 1")
    private Integer maxParticipants;

    private String description;

    /** Optional logical reference to a Scheduling Service time slot. */
    private UUID preferredTimeSlotId;

    /** Optional logical reference to a Booking Service booking record. */
    private UUID facilityBookingId;
}