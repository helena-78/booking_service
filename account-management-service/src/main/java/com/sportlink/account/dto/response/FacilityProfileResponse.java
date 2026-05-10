package com.sportlink.account.dto.response;

import com.sportlink.account.model.enums.FacilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityProfileResponse {

    private UUID centerId;
    private String name;
    private String email;
    private String sportTypes;
    private String contactName;
    private String contactPhone;
    private String website;
    private String city;
    private String district;
    private String coordinates;
    private FacilityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
