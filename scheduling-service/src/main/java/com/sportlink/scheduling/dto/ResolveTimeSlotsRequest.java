package com.sportlink.scheduling.dto;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Given a set of user IDs (and optionally a facility ID), find overlapping available times
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResolveTimeSlotsRequest {

    @NotNull(message = "userIds is required")
    @NotEmpty(message = "userIds must contain at least one user")
    private List<UUID> userIds;

    /** when set, the facility's own schedule is also intersected. */
    private UUID facilityId;
}