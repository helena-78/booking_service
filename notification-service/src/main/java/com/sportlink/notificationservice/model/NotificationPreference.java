package com.sportlink.notificationservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "notification_preferences",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_type", "channel"})
)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationPreference {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private NotificationType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;
}
