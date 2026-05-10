package com.sportlink.account.dto.request;

import com.sportlink.account.model.enums.SkillLevel;
import com.sportlink.account.model.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    private String phone;

    private String other;

    private UserRole role;

    private SkillLevel skillLevel;

    private String language;

    private String sportPreferences;

    private String city;

    private String district;

    private UUID availabilityId;
}
