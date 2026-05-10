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
@Schema(description = "A comment posted on an activity")
public class CommentResponse {

    @Schema(description = "Unique comment ID")
    private UUID id;

    @Schema(description = "ID of the activity this comment belongs to")
    private UUID activityId;

    @Schema(description = "ID of the user who posted this comment")
    private UUID authorId;

    @Schema(description = "Text content of the comment")
    private String body;

    @Schema(description = "When the comment was posted")
    private LocalDateTime createdAt;

    @Schema(description = "When the comment was last edited")
    private LocalDateTime updatedAt;
}
