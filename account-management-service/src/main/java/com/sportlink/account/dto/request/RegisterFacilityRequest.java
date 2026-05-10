package com.sportlink.account.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFacilityRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    private String sportTypes;

    private String contactName;

    private String contactPhone;

    private String website;

    @NotBlank
    private String city;

    private String district;

    private String coordinates;
}
