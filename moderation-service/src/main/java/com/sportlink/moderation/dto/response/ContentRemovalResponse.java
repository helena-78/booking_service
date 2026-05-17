package com.sportlink.moderation.dto.response;

import com.sportlink.moderation.model.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentRemovalResponse {

    private UUID contentId;
    private ContentType contentType;
    private boolean removed;
    private String message;
}
