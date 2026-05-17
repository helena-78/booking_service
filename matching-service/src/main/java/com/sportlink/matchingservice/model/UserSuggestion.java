package com.sportlink.matchingservice.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.sportlink.matchingservice.dto.UserSuggestionResponse;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "user_suggestions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSuggestion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "suggestion_id")
    private UUID suggestionId;

    /**
     * The user who owns this suggestion list — i.e., suggestions are computed FOR this user.
     * Logical cross-service ref to Account Management users.userId.
     */
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * Array of {userId, compatibilityScore} objects stored as JSONB.
     * Each element is a SuggestedUser value object snapshot.
     */
    @Type(JsonBinaryType.class)
    @Column(name = "suggested_users", columnDefinition = "jsonb", nullable = false)
    private List<SuggestedUser> suggestedUsers;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UserSuggestionResponse toResponse() {
        return UserSuggestionResponse.builder()
                .suggestionId(this.suggestionId)
                .userId(this.userId)
                .suggestedUsers(this.suggestedUsers)
                .createdAt(this.createdAt)
                .build();
    }
}
