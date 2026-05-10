package com.sportlink.interactionservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for posting or editing a comment")
public class CommentRequest {

    @NotBlank(message = "Comment body must not be blank")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    @Schema(description = "Text content of the comment", example = "Great activity, looking forward to it!")
    private String body;

    @Schema(description = "UUID of the activity owner — used to notify them of the new comment", example = "11111111-1111-1111-1111-111111111111")
    private UUID activityOwnerId;
}
