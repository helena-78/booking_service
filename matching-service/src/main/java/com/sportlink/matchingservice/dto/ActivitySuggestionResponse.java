package com.sportlink.matchingservice.dto;

import com.sportlink.matchingservice.model.SuggestedActivity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ActivitySuggestionResponse {
    private UUID suggestionId;
    private UUID userId;
    private List<SuggestedActivity> suggestedActivities;
    private LocalDateTime createdAt;
}
