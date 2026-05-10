package com.sportlink.scheduling.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The list replaces the user's current availability windows entirely
 * @Valid on the list triggers validation of each AvailabilityWindowDto inside.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAvailabilityRequest {

    @NotNull(message = "windows list is required")
    @Valid
    private List<AvailabilityWindowDto> windows;
}