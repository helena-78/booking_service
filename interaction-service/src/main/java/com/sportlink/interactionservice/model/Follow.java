package com.sportlink.interactionservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "follows",
    uniqueConstraints = @UniqueConstraint(columnNames = {"follower_id", "followee_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "follower_id", nullable = false)
    private UUID followerId;

    @Column(name = "followee_id", nullable = false)
    private UUID followeeId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
