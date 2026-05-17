package com.sportlink.matchingservice.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sportlink.matchingservice.dto.ActivitySuggestionResponse;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "activity_suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivitySuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "suggestion_id")
    private UUID suggestionId;

    /**
     * The user who receives these activity suggestions.
     * Logical cross-service ref to Account Management users.userId.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Array of {activityId, relevanceScore} objects stored as JSONB.
     * Each element is a SuggestedActivity value object snapshot.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "suggested_activities", columnDefinition = "jsonb", nullable = false)
    private List<SuggestedActivity> suggestedActivities;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public ActivitySuggestionResponse toResponse() {
        return ActivitySuggestionResponse.builder()
                .suggestionId(this.suggestionId)
                .userId(this.userId)
                .suggestedActivities(this.suggestedActivities)
                .createdAt(this.createdAt)
                .build();
    }
}
