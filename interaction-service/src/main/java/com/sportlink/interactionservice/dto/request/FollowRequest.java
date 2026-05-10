package com.sportlink.interactionservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body to follow another user")
public class FollowRequest {

    @NotNull(message = "targetUserId is required")
    @Schema(description = "ID of the user to follow", example = "22222222-2222-2222-2222-222222222222")
    private UUID targetUserId;
}
