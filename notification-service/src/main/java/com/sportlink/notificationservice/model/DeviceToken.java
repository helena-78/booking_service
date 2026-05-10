package com.sportlink.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "device_tokens")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DeviceToken {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false, updatable = false)
    private LocalDateTime registeredAt;
}
