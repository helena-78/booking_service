package com.sportlink.interactionservice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Represents a follow relationship between two users")
public class FollowResponse {

    @Schema(description = "ID of the user who followed")
    private UUID followerId;

    @Schema(description = "ID of the user being followed")
    private UUID followeeId;

    @Schema(description = "Timestamp when the follow was created")
    private LocalDateTime createdAt;
}
