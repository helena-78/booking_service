package com.sportlink.ratingservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.sportlink.ratingservice.dto.UserReputationResponse;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_reputations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReputation {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "average_score")
    private double averageScore;

    @Column(name = "user_label")
    private String userLabel;

    @Column(name = "total_ratings")
    private int totalRatings;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    public UserReputationResponse toResponse() {
        return UserReputationResponse.builder()
                .userId(this.userId)
                .averageScore(this.averageScore)
                .userLabel(this.userLabel)
                .totalRatings(this.totalRatings)
                .lastUpdatedAt(this.lastUpdatedAt)
                .build();
    }
}
