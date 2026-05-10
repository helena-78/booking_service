package com.sportlink.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProjection {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String username;
}
