package com.sportlink.moderation.service;

import com.sportlink.moderation.dto.response.ContentRemovalResponse;
import com.sportlink.moderation.model.enums.ContentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentRemovalService {

    public ContentRemovalResponse delegateRemoval(ContentType contentType, UUID contentId) {
        String owningService = switch (contentType) {
            case COMMENT -> "Interaction Service";
            case ACTIVITY_DESCRIPTION -> "Activity Management Service";
            case USER_PROFILE -> "Account Management Service";
        };
        log.info("Delegating removal of {} content {} to {}", contentType, contentId, owningService);
        return ContentRemovalResponse.builder()
                .contentId(contentId)
                .contentType(contentType)
                .removed(true)
                .message("Removal delegated to " + owningService)
                .build();
    }
}
