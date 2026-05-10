package com.sportlink.account.dto.response;

import com.sportlink.account.model.enums.SkillLevel;
import com.sportlink.account.model.enums.UserRole;
import com.sportlink.account.model.enums.UserStatus;
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
public class UserProfileResponse {

    private UUID userId;
    private String name;
    private String email;
    private String phone;
    private String other;
    private UserRole role;
    private UserStatus status;
    private SkillLevel skillLevel;
    private String language;
    private String sportPreferences;
    private String city;
    private String district;
    private UUID availabilityId;
    private String behaviorLabel;
    private String labelValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
