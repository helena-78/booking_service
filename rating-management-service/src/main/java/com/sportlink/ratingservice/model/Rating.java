package com.sportlink.ratingservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sportlink.ratingservice.dto.RatingResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rating_id")
    private UUID ratingId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(name = "reviewee_id", nullable = false)
    private UUID revieweeId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    // BehaviorScore value object (embedded as columns)
    @Column(name = "behavior_value", nullable = false)
    private int behaviorValue;

    @Column(name = "behavior_label")
    private String behaviorLabel;

    // SkillScore value object (embedded as columns)
    @Column(name = "skill_value", nullable = false)
    private int skillValue;

    @Column(name = "skill_label")
    private String skillLabel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public RatingResponse toResponse() {
        return RatingResponse.builder()
                .ratingId(this.ratingId)
                .reviewerId(this.reviewerId)
                .revieweeId(this.revieweeId)
                .activityId(this.activityId)
                .behaviorValue(this.behaviorValue)
                .behaviorLabel(this.behaviorLabel)
                .skillValue(this.skillValue)
                .skillLabel(this.skillLabel)
                .createdAt(this.createdAt)
                .build();
    }
}