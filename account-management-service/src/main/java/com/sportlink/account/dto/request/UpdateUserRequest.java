package com.sportlink.account.dto.request;

import com.sportlink.account.model.enums.SkillLevel;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 200)
    private String name;

    private String phone;

    private String other;

    private SkillLevel skillLevel;

    private String language;

    private String sportPreferences;

    private String city;

    private String district;
}
