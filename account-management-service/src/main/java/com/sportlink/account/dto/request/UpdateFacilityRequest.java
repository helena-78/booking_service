package com.sportlink.account.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFacilityRequest {

    @Size(max = 255)
    private String name;

    private String contactName;

    private String contactPhone;

    private String website;

    private String sportTypes;

    private String city;

    private String district;

    private String coordinates;
}
