package com.sportlink.matchingservice.dto;

import com.sportlink.matchingservice.model.SuggestedUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserSuggestionResponse {
    private UUID suggestionId;
    private UUID userId;
    private List<SuggestedUser> suggestedUsers;
    private LocalDateTime createdAt;
}
