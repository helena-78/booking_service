package com.sportlink.moderation.dto.request;

import com.sportlink.moderation.model.enums.ContentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCaseRequest {

    @NotNull
    private UUID reportedContentId;

    @NotNull
    private ContentType contentType;

    @NotNull
    private UUID reportedByUserId;

    @NotNull
    private UUID targetUserId;

    @NotBlank
    private String content;
}
