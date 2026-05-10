package com.sportlink.activitymanagement.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateActivityRequest {

    private String title;
    private String description;
    private UUID preferredTimeSlotId;

    @Min(value = 1, message = "maxParticipants must be at least 1")
    private Integer maxParticipants;
}