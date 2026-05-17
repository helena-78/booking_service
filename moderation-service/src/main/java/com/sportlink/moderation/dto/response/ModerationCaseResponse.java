package com.sportlink.moderation.dto.response;

import com.sportlink.moderation.model.enums.ContentType;
import com.sportlink.moderation.model.enums.Verdict;
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
public class ModerationCaseResponse {

    private UUID caseId;
    private UUID reportedContentId;
    private ContentType contentType;
    private UUID reportedByUserId;
    private UUID targetUserId;
    private String content;
    private Verdict verdict;
    private UUID moderatorId;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
