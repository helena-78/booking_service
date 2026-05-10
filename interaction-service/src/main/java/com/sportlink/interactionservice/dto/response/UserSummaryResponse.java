package com.sportlink.interactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Minimal user identity used in follower/following lists")
public class UserSummaryResponse {

    @Schema(description = "User ID")
    private UUID userId;

    @Schema(description = "Username")
    private String username;
}
