package com.sportlink.notificationservice.repository;

import com.sportlink.notificationservice.model.Channel;
import com.sportlink.notificationservice.model.NotificationPreference;
import com.sportlink.notificationservice.model.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, UUID> {
    List<NotificationPreference> findByUserId(UUID userId);
    Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(UUID userId, NotificationType eventType, Channel channel);
}
