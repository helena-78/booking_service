package com.sportlink.account.model.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FacilityLocation {

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "district")
    private String district;

    @Column(name = "coordinates")
    private String coordinates;
}
