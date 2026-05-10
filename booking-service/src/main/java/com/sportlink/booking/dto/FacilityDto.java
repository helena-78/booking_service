package com.sportlink.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * represents a bookable facility, returned from
 * GET /api/bookings/facilities/search.
 *
 * The shape mirrors what the Search Service returns from its SearchIndex
 * (entityType=FACILITY) - displayName, sportType, location, facilityName.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacilityDto {
    private String entityId;
    private String facilityName;
    private String displayName;
    private String sportType;
    private String location;
}
